package watch.craft.executor

import mu.KotlinLogging
import watch.craft.*
import watch.craft.enrichers.Categoriser
import watch.craft.enrichers.Newalyser
import watch.craft.executor.ScraperAdapter.Result
import watch.craft.storage.CachingGetter
import java.time.Clock
import java.time.Instant

class Executor(
  rateLimitPeriodMillis: Int = 10,
  private val results: ResultsManager,
  private val getter: CachingGetter,
  private val clock: Clock = Clock.systemUTC()
) {
  private val logger = KotlinLogging.logger {}
  private val rawExecutor = ConcurrentRawScraperExecutor(rateLimitPeriodMillis = rateLimitPeriodMillis)

  fun scrape(scrapers: Collection<Scraper>): Inventory {
    val now = clock.instant()

    val items = scrapers
      .execute()
      .normalise()
      .categorise()
      .newalyse(now)
      .sort()
      .bestPricedItems()
      .also { it.logStats() }

    return Inventory(
      metadata = Metadata(capturedAt = now),
      categories = CATEGORY_KEYWORDS.keys.toList(),
      breweries = scrapers.map { it.brewery },
      items = items
    )
  }

  private fun Collection<Scraper>.execute() = rawExecutor.execute(map { ScraperAdapter(getter, it) })

  private fun Collection<Result>.normalise() = mapNotNull {
    try {
      it.normalise()
    } catch (e: InvalidItemException) {
      logger.warn("[${it.breweryName}] Invalid item [${it.entry.rawName}]", e)
      null
    } catch (e: Exception) {
      logger.warn("[${it.breweryName}] Unexpected error while validating [${it.entry.rawName}]", e)
      null
    }
  }

  private fun Collection<Item>.categorise() = map(Categoriser(CATEGORY_KEYWORDS)::enrich)

  private fun Collection<Item>.newalyse(now: Instant) = map(Newalyser(results, now)::enrich)

  private fun Collection<Item>.sort() = sortedWith(
    compareBy(
      { it.brewery },
      { it.name },
      { it.available },
      { it.sizeMl },
      { it.keg },
      { it.numItems }
    )
  )

  private fun List<Item>.bestPricedItems() = groupBy { ItemGroupFields(it.brewery, it.name, it.keg) }
    .map { (key, group) ->
      if (group.size > 1) {
        logger.info("[${key.brewery}] Eliminating ${group.size - 1} more expensive item(s) for [${key.name}]")
      }
      group.minBy { it.perItemPrice }!!
    }

  private fun List<Item>.logStats() {
    groupBy { it.brewery }.forEach { (key, group) -> logger.info("Scraped (${key}): ${group.size}") }
    logger.info("Scraped (TOTAL): ${size}")
  }

  private data class ItemGroupFields(
    val brewery: String,
    val name: String,
    val keg: Boolean
  )
}
