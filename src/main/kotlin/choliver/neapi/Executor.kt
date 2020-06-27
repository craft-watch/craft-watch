package choliver.neapi

import choliver.neapi.Scraper.IndexEntry
import choliver.neapi.getters.Getter
import choliver.neapi.getters.HtmlGetter
import mu.KotlinLogging
import java.util.stream.Collectors.toList

class Executor(getter: Getter<String>) {
  private val jsonGetter = HtmlGetter(getter)
  private val logger = KotlinLogging.logger {}

  fun scrape(vararg scrapers: Scraper) = Inventory(
    items = scrapers.flatMap { ScraperExecutor(it).execute() }
      .also { it.logStats() }
  )

  inner class ScraperExecutor(private val scraper: Scraper) {
    private val brewery = scraper.name

    fun execute() = scrapeIndexSafely(scraper)
      .parallelStream()
      .map { scrapeItemSafely(it) }
      .collect(toList())
      .filterNotNull()
      .bestPricedItems()

    private fun scrapeIndexSafely(scraper: Scraper): List<IndexEntry> {
      logger.info("[${brewery}] Scraping brewery")
      return try {
        scraper.scrapeIndex(jsonGetter.request(scraper.rootUrl))
      } catch (e: NonFatalScraperException) {
        logger.warn("[${brewery}] Error scraping brewery", e)
        emptyList()
      } catch (e: FatalScraperException) {
        logger.error("[${brewery}] Fatal error scraping brewery", e)
        throw e
      } catch (e: Exception) {
        logger.warn("[${brewery}] Unexpected error scraping brewery", e)
        emptyList()
      }
    }

    private fun scrapeItemSafely(entry: IndexEntry): Item? {
      logger.info("[${brewery}] Scraping [${entry.rawName}]")
      return try {
        entry
          .scrapeItem(jsonGetter.request(entry.url))
          .normalise(brewery, entry.url)
      } catch (e: SkipItemException) {
        logger.info("[${brewery}] Skipping [${entry.rawName}] because: ${e.message}")
        null
      } catch (e: NonFatalScraperException) {
        logger.warn("[${brewery}] Error scraping [${entry.rawName}]", e)
        null
      } catch (e: FatalScraperException) {
        logger.error("[${brewery}] Error scraping [${entry.rawName}]", e)
        throw e
      } catch (e: Exception) {
        logger.warn("[${brewery}] Unexpected error scraping [${entry.rawName}]", e)
        null
      }
    }

    private fun List<Item>.bestPricedItems() = groupBy { ItemGroupFields(it.name, it.keg) }
      .map { (key, group) ->
        if (group.size > 1) {
          logger.info("[${brewery}] Eliminating ${group.size - 1} more expensive item(s) for [${key}]")
        }
        group.minBy { it.perItemPrice }!!
      }
  }

  private fun List<Item>.logStats() {
    groupBy { it.brewery }.forEach { (key, group) -> logger.info("Scraped (${key}): ${group.size}") }
    logger.info("Scraped (TOTAL): ${size}")
  }

  private data class ItemGroupFields(
    val name: String,
    val keg: Boolean
  )
}
