package watch.craft.executor

import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import watch.craft.*
import watch.craft.enrichers.Categoriser
import watch.craft.enrichers.Newalyser
import watch.craft.executor.ScraperAdapter.Result
import watch.craft.executor.ScraperAdapter.StatsWith
import watch.craft.network.Retriever
import java.time.Clock
import java.time.Instant

class Executor(
  private val results: ResultsManager,
  private val createRetriever: (name: String) -> Retriever,
  private val clock: Clock = Clock.systemUTC()
) {
  private val logger = KotlinLogging.logger {}

  fun scrape(scrapers: Collection<Scraper>): Inventory {
    val now = clock.instant()

    return scrapers
      .execute()
      .normalise()
      .toInventory(scrapers, now)
      .consolidateOffers()
      .sortItems()
      .enrichWith(Categoriser(CATEGORY_KEYWORDS))
      .enrichWith(Newalyser(results, now))
      .also { it.logStats() }
  }

  private fun Collection<Scraper>.execute() = runBlocking {
    this@execute
      .map { async { it.execute() } }
      .map { it.await() }
  }

  private suspend fun Scraper.execute() = createRetriever(brewery.shortName).use {
    ScraperAdapter(it, this).execute()
  }

  private fun Collection<StatsWith<Result>>.normalise() = map {
    var stats = it.stats
    val entries = it.entries.mapNotNull { result ->
      try {
        result.normalise()
      } catch (e: InvalidItemException) {
        logger.warn("[${result.breweryName}] Invalid item [${result.rawName}]", e)
        stats = stats.copy(numInvalid = stats.numInvalid + 1)
        null
      } catch (e: Exception) {
        logger.warn("[${result.breweryName}] Unexpected error while validating [${result.rawName}]", e)
        stats = stats.copy(numErrors = stats.numErrors + 1)
        null
      }
    }
    StatsWith(entries, stats)
  }

  private fun Collection<StatsWith<Item>>.toInventory(scrapers: Collection<Scraper>, now: Instant) = Inventory(
    metadata = Metadata(capturedAt = now),
    stats = Stats(
      breweries = map { it.stats }
    ),
    categories = CATEGORY_KEYWORDS.keys.toList(),
    breweries = scrapers.map { it.brewery },
    items = flatMap { it.entries }
  )

  private fun Inventory.enrichWith(enricher: Enricher) = copy(
    items = items.map(enricher::enrich),
    breweries = breweries.map(enricher::enrich)
  )

  private fun Inventory.sortItems() = copy(items = items
    .sortedWith(compareBy(
      { it.brewery },
      { it.name }
    ))
  )

  private fun Inventory.logStats() {
    stats.breweries
      .forEach { breweryStats -> logger.info("Scraped (${breweryStats.name}): ${breweryStats.numItems}") }
    logger.info("Scraped (TOTAL): ${stats.breweries.sumBy { it.numItems }}")
  }
}
