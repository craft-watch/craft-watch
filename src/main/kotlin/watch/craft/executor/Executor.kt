package watch.craft.executor

import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import watch.craft.*
import watch.craft.executor.ScraperAdapter.Result
import watch.craft.network.Retriever
import java.time.Clock

class Executor(
  private val results: ResultsManager,
  private val createRetriever: (name: String) -> Retriever,
  private val clock: Clock = Clock.systemUTC()
) {
  fun scrape(scrapers: Collection<ScraperEntry>) = Context(scrapers).scrape()

  private inner class Context(private val scrapers: Collection<ScraperEntry>) {
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
        .map { async { it.scraper.execute() } }
        .map { it.await() }
    }

    private suspend fun Scraper.execute() = createRetriever(brewery.shortName).use {
      ScraperAdapter(it, this).execute()
    }

    private fun StatsWith<Result>.postProcessItems(): StatsWith<Item> {
      val consolidated = this
        .normaliseToItems()
        .consolidateOffers()
      return consolidated
        .copy(entries = consolidated.entries
          .sortedBy { it.name }
          .map(categoriser::enrich)
          .map(newalyser::enrich)
        )
    }
  }
}
