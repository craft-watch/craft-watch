package watch.craft.executor

import mu.KotlinLogging
import watch.craft.*
import watch.craft.enrichers.Categoriser
import watch.craft.enrichers.Newalyser
import watch.craft.executor.ScraperExecutor.Result
import watch.craft.storage.CachingGetter
import java.time.Clock
import java.time.Instant
import java.util.concurrent.Executors.newFixedThreadPool

class Executor(
  private val results: ResultsManager,
  private val getter: CachingGetter,
  private val clock: Clock = Clock.systemUTC()
) {
  private val logger = KotlinLogging.logger {}

  private val executor = newFixedThreadPool(4)

  fun scrape(vararg scrapers: Scraper): Inventory {
    val now = clock.instant()

    val adapters = scrapers.map { ScraperExecutor(getter, it) }

    val indexTasks = adapters.flatMap { it.indexTasks }

    val itemTasks = indexTasks
      .map { executor.submit(it) }
      .flatMap { it.get() }

    val items = itemTasks
      .map { executor.submit(it) }
      .mapNotNull { it.get() }
      .normalise()
      .categorise()
      .newalyse(now)
      .bestPricedItems()
      .sort()
      .also { it.logStats() }

    return Inventory(
      metadata = Metadata(capturedAt = now),
      categories = CATEGORY_KEYWORDS.keys.toList(),
      items = items
    )
  }

  private fun List<Result>.normalise() = mapNotNull {
    try {
      it.normalise()
    } catch (e: InvalidItemException) {
      logger.warn("[${it.brewery}] Invalid item [${it.entry.rawName}]", e)
      null
    } catch (e: Exception) {
      logger.warn("[${it.brewery}] Unexpected error while validating [${it.entry.rawName}]", e)
      null
    }
  }

  private fun List<Item>.categorise() = map(Categoriser(CATEGORY_KEYWORDS)::enrich)

  private fun List<Item>.newalyse(now: Instant) = map(Newalyser(results, now)::enrich)

  private fun List<Item>.bestPricedItems() = groupBy { ItemGroupFields(it.brewery, it.name, it.keg) }
    .map { (key, group) ->
      if (group.size > 1) {
        logger.info("[${key.brewery}] Eliminating ${group.size - 1} more expensive item(s) for [${key.name}]")
      }
      group.minBy { it.perItemPrice }!!
    }

  private fun List<Item>.sort() = sortedBy { it.name }.sortedBy { it.brewery }

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
