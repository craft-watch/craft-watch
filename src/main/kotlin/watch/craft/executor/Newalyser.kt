package watch.craft.executor

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import watch.craft.Brewery
import watch.craft.Item
import watch.craft.MinimalItem
import watch.craft.ResultsManager
import java.time.Instant
import java.time.temporal.ChronoUnit.DAYS

class Newalyser(
  private val results: ResultsManager,
  private val now: Instant
) {
  private val logger = KotlinLogging.logger {}

  private val itemAppearances by lazy {
    results.listHistoricalResults()
      .filter { DAYS.between(it, now).toInt() in 0..MAX_DAYS_AGO }
      .collateInventory()
      .map { (item, instant) -> item.copy(name = item.name.toLowerCase()) to instant }
  }

  private val oldItems by lazy {
    itemAppearances
      .filter { (_, instant) -> DAYS.between(instant, now) >= MIN_DAYS_AGO }
      .map { (item, _) -> item }
      .distinct()
  }

  private val oldBreweries by lazy {
    oldItems
      .map { it.brewery }
      .distinct()
      .toSet()
  }

  fun enrich(item: Item) = item.copy(
    new = MinimalItem(brewery = item.brewery, name = item.name.toLowerCase()) !in oldItems
  )

  fun enrich(brewery: Brewery) = brewery.copy(
    new = brewery.shortName !in oldBreweries
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

  companion object {
    // TODO - modify range once we have more data
    private const val MIN_DAYS_AGO = 3
    private const val MAX_DAYS_AGO = 14
  }
}
