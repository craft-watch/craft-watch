package watch.craft.executor

import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import watch.craft.*
import watch.craft.enrichers.Categoriser
import watch.craft.enrichers.Newalyser
import watch.craft.executor.ScraperAdapter.Result
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
      .addStats()
      .also { it.logStats() }
  }

  private fun Collection<Scraper>.execute() = runBlocking {
    this@execute
      .map { async { it.execute() } }
      .flatMap { it.await() }
  }

  private suspend fun Scraper.execute() = createRetriever(brewery.shortName).use {
    ScraperAdapter(it, this).execute()
  }

  private fun Collection<Result>.normalise() = mapNotNull {
    try {
      it.normalise()
    } catch (e: InvalidItemException) {
      logger.warn("[${it.breweryName}] Invalid item [${it.rawName}]", e)
      null
    } catch (e: Exception) {
      logger.warn("[${it.breweryName}] Unexpected error while validating [${it.rawName}]", e)
      null
    }
  }

  private fun Collection<Item>.toInventory(scrapers: Collection<Scraper>, now: Instant) = Inventory(
    metadata = Metadata(capturedAt = now),
    categories = CATEGORY_KEYWORDS.keys.toList(),
    breweries = scrapers.map { it.brewery },
    items = toList()
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

  private fun Inventory.addStats() = copy(
    stats = Stats(
      breweries = items
        .groupBy { it.brewery }
        .map { (name, items) ->
          BreweryStats(
            name = name,
            numItems = items.size
          )
        }
    )
  )

  private fun Inventory.logStats() {
    stats.breweries
      .forEach { breweryStats -> logger.info("Scraped (${breweryStats.name}): ${breweryStats.numItems}") }
    logger.info("Scraped (TOTAL): ${stats.breweries.sumBy { it.numItems }}")
  }
}
