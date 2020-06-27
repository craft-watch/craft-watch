package choliver.neapi

import choliver.neapi.Scraper.IndexEntry
import choliver.neapi.getters.Getter
import choliver.neapi.getters.HtmlGetter
import mu.KotlinLogging

class Executor(getter: Getter<String>) {
  private val jsonGetter = HtmlGetter(getter)
  private val logger = KotlinLogging.logger {}

  fun scrape(vararg scrapers: Scraper) = Inventory(
    items = scrapers.flatMap { ScraperExecutor(it).execute() }
  )

  inner class ScraperExecutor(private val scraper: Scraper) {
    private val brewery = scraper.name

    fun execute() = scrapeIndexSafely(scraper)
      .mapNotNull { scrapeItemSafely(it) }
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
      logger.info("[${brewery}] Scraping item: ${entry.rawName}")
      return try {
        entry
          .scrapeItem(jsonGetter.request(entry.url))
          .normalise(brewery, entry.url)
      } catch (e: SkipItemException) {
        logger.info("[${brewery}] Skipping item because: ${e.message}")
        null
      } catch (e: NonFatalScraperException) {
        logger.warn("[${brewery}] Error scraping item: ${entry.rawName}", e)
        null
      } catch (e: FatalScraperException) {
        logger.error("[${brewery}] Error scraping item: ${entry.rawName}", e)
        throw e
      } catch (e: Exception) {
        logger.warn("[${brewery}] Unexpected error scraping item: ${entry.rawName}", e)
        null
      }
    }

    private fun List<Item>.bestPricedItems() = groupBy { it.name to it.summary }
      .map { (key, group) ->
        if (group.size > 1) {
          logger.info("[${brewery}] Eliminating ${group.size - 1} more expensive item(s) for: ${key}")
        }
        group.minBy { it.perItemPrice }!!
      }
  }
}
