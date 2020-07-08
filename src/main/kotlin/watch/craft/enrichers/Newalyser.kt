package watch.craft.enrichers

import mu.KotlinLogging
import watch.craft.*
import java.time.Instant
import java.time.temporal.ChronoUnit.DAYS

class Newalyser(
  private val results: ResultsManager,
  private val now: Instant
) : Enricher {
  private val logger = KotlinLogging.logger {}


  private val oldItems = results.listHistoricalResults()
    .filter { DAYS.between(it, now).toInt() in MIN_DAYS_AGO..MAX_DAYS_AGO }
    .flatMap {
      logger.info("Collating old inventory from: ${it}")
      results.readMinimalHistoricalResult(it).items
    }
    .map { it.copy(name = it.name.toLowerCase()) }
    .distinct()
    .toSet()
    .onEach { logger.info("Historical item: ${it}") }

  private val oldBreweries = oldItems
    .map { it.brewery }
    .distinct()
    .toSet()

  override fun enrich(item: Item) = item.copy(
    new = MinimalItem(brewery = item.brewery, name = item.name.toLowerCase()) !in oldItems
  )

  override fun enrich(brewery: Brewery) = brewery.copy(
    new = brewery.shortName !in oldBreweries
  )

  companion object {
    // TODO - modify range once we have more data
    private const val MIN_DAYS_AGO = 3
    private const val MAX_DAYS_AGO = 14
  }
}
