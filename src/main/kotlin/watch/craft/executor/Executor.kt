package watch.craft.executor

import mu.KotlinLogging
import watch.craft.Inventory
import watch.craft.Item
import watch.craft.Metadata
import watch.craft.Scraper
import watch.craft.storage.CachingGetter
import java.time.Clock

class Executor(
  private val getter: CachingGetter,
  private val clock: Clock = Clock.systemUTC()
) {
  private val logger = KotlinLogging.logger {}

  fun scrape(vararg scrapers: Scraper): Inventory {
    val items = scrapers
      .flatMap { ScraperExecutor(getter, it).execute() }
      .bestPricedItems()
      .also { it.logStats() }

    return Inventory(
      metadata = Metadata(
        capturedAt = clock.instant()
      ),
      items = items
    )
  }

  private fun List<Item>.logStats() {
    groupBy { it.brewery }.forEach { (key, group) -> logger.info("Scraped (${key}): ${group.size}") }
    logger.info("Scraped (TOTAL): ${size}")
  }

  private fun List<Item>.bestPricedItems() = groupBy { ItemGroupFields(it.brewery, it.name, it.keg) }
    .map { (key, group) ->
      if (group.size > 1) {
        logger.info("[${key.brewery}] Eliminating ${group.size - 1} more expensive item(s) for [${key.name}]")
      }
      group.minBy { it.perItemPrice }!!
    }

  private data class ItemGroupFields(
    val brewery: String,
    val name: String,
    val keg: Boolean
  )
}
