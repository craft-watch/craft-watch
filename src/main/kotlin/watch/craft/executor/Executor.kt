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
import kotlin.math.round

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
      .mergeItems()
      .sortItems()
      .enrichWith(Categoriser(CATEGORY_KEYWORDS))
      .enrichWith(Newalyser(results, now))
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

  private fun Inventory.mergeItems() =
    copy(items = items.groupBy { ItemGroupFields(it.brewery, it.name.toLowerCase()) }
      .map { (key, group) ->
        if (group.size > 1) {
          logger.info("[${key.brewery}] Merging ${group.size} item(s) for [${key.name}]")
        }

        val offers = group.mergeAndPrioritiseOffers()

        val headlineItem = offers.first().item

        headlineItem.copy(offers = offers.map { it.offer })
      }
    )

  // TODO - need a stable notion of "first" - will need to sort upstream
  // TODO - fill in missing fields from non-archetypes
  // TODO - do we want a URL per offer?  Keg vs. can vs. item may be different pages
  private fun List<Item>.mergeAndPrioritiseOffers() =
    flatMap { item -> item.offers.map { ItemAndOffer(item, it) } }
      .distinctBy { round(it.offer.pricePerMl() * 100) } // Work in pence to avoid FP precision issues
      .sortedWith(compareBy(
        { it.offer.keg },   // Kegs should be lowest priority
        { it.offer.pricePerMl() },
        { it.offer.quantity } // All being equal, we prefer to buy fewer cans
      ))

  private data class ItemAndOffer(
    val item: Item,
    val offer: Offer
  )

  private fun Inventory.sortItems() = copy(items = items
    .sortedWith(compareBy(
      { it.brewery },
      { it.name }
    ))
  )

  private fun Inventory.logStats() {
    items.groupBy { it.brewery }
      .forEach { (key, group) -> logger.info("Scraped (${key}): ${group.size}") }
    logger.info("Scraped (TOTAL): ${items.size}")
  }

  private fun Offer.pricePerMl() = totalPrice / (quantity * (sizeMl ?: DEFAULT_SIZE_ML))

  private data class ItemGroupFields(
    val brewery: String,
    val name: String
  )
}
