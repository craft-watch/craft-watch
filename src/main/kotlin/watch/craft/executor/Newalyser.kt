package watch.craft.executor

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import watch.craft.Brewery
import watch.craft.Item
import watch.craft.MinimalItem
import watch.craft.ResultsManager
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit.DAYS
import java.time.temporal.ChronoUnit.HOURS

class Newalyser(
  private val results: ResultsManager,
  private val now: Instant
) {
  private val logger = KotlinLogging.logger {}

  private data class Interval(
    val min: Instant,
    val max: Instant
  )

  private val window = Interval(
    min = now - Duration.of(MAX_DAYS_AGO.toLong(), DAYS),
    max = now - Duration.of(MIN_DAYS_AGO.toLong(), DAYS)
  )

  private val itemAppearances by lazy {
    results.listHistoricalResults()
      .filter { DAYS.between(it, now).toInt() in 0..MAX_DAYS_AGO }
      .collateInventory()
      .map { (item, instant) -> item.copy(name = item.name.toLowerCase()) to instant }
  }

  private val itemLifetimes by lazy {
    itemAppearances
      .extractLifetimePerKey()
  }

  private val breweryLifetimes by lazy {
    itemAppearances
      .map { (item, instant) -> item.brewery to instant }
      .extractLifetimePerKey()
  }

  fun enrich(item: Item): Item {
    val minimalItem = MinimalItem(brewery = item.brewery, name = item.name.toLowerCase())
    return item.copy(
      new = isNew(minimalItem, itemLifetimes)
        && HOURS.between(
          breweryLifetimes[item.brewery]?.min ?: Instant.MIN,
          itemLifetimes[minimalItem]?.min ?: Instant.MIN
        ) > 6
    )
  }

  fun enrich(brewery: Brewery) = brewery.copy(
    new = isNew(brewery.shortName, breweryLifetimes)
  )

  private fun List<Instant>.collateInventory() = runBlocking {
    this@collateInventory
      .map { instant ->
        async(Dispatchers.IO) {
          logger.info("Collating old inventory from: ${instant}")
          results.readMinimalHistoricalResult(instant).items.map { item -> item to instant }
        }
      }
      .flatMap { it.await() }
  }

  private fun <T> isNew(entry: T, lifetimes: Map<T, Interval>) =
    lifetimes[entry]?.let { !(it overlaps window) } ?: true

  private infix fun Interval.overlaps(rhs: Interval) = (min <= rhs.max) && (rhs.min <= max)

  private fun <T> List<Pair<T, Instant>>.extractLifetimePerKey() = this
    .groupBy { it.first }
    .mapValues { (_, pairs) ->
      val instants = pairs.map { it.second }
      Interval(min = instants.min()!!, max = instants.max()!!)
    }

  companion object {
    // TODO - modify range once we have more data
    private const val MIN_DAYS_AGO = 3
    private const val MAX_DAYS_AGO = 14
  }
}
