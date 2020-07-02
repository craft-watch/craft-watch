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
}
