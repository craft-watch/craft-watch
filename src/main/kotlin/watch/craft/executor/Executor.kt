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

class Executor(
  private val results: ResultsManager,
  private val createRetriever: (name: String) -> Retriever,
  private val clock: Clock = Clock.systemUTC()
) {
  private val logger = KotlinLogging.logger {}

  fun scrape(scrapers: Collection<Scraper>) = Context(scrapers).scrape()

  private inner class Context(private val scrapers: Collection<Scraper>) {
    private val now = clock.instant()
    private val categoriser = Categoriser(CATEGORY_KEYWORDS)
    private val newalyser = Newalyser(results, now)

    fun scrape(): Inventory {
      val breweryItems = executeAllInParallel()
        .map { it.postProcessItems() }

      val breweries = scrapers
        .map { it.brewery }
        .map { newalyser.enrich(it) }

      return Inventory(
        metadata = Metadata(capturedAt = now),
        stats = Stats(
          breweries = breweryItems.map { it.stats }
        ),
        categories = CATEGORY_KEYWORDS.keys.toList(),
        breweries = breweries,
        items = breweryItems.flatMap { it.entries }
      )
    }

    private fun executeAllInParallel() = runBlocking {
      scrapers
        .map { async { it.execute() } }
        .map { it.await() }
    }

    private suspend fun Scraper.execute() = createRetriever(brewery.shortName).use {
      ScraperAdapter(it, this).execute()
    }

    private fun StatsWith<Result>.postProcessItems(): StatsWith<Item> {
      val items = normalise()

      return items.copy(entries = items.entries
        .consolidateOffers()
        .sortedBy { it.name }
        .map(categoriser::enrich)
        .map(newalyser::enrich)
      )
    }

    private fun StatsWith<Result>.normalise(): StatsWith<Item> {
      var stats = stats
      val entries = entries.mapNotNull { result ->
        try {
          result.normalise()
        } catch (e: InvalidItemException) {
          logger.warn("[${result.breweryName}] Invalid item [${result.rawName}]", e)
          stats = stats.copy(numInvalid = stats.numInvalid + 1)
          null
        }
      }
      return StatsWith(entries, stats)
    }
  }
}
