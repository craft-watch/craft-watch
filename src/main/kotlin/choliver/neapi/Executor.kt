package choliver.neapi

import choliver.neapi.Scraper.IndexEntry
import choliver.neapi.Scraper.Result
import mu.KotlinLogging

class Executor(getter: HttpGetter) {
  private val jsonGetter = JsonGetter(getter)
  private val logger = KotlinLogging.logger {}

  fun scrapeAll(vararg scrapers: Scraper) = Inventory(
    items = scrapers.flatMap { scraper -> scrapeBrewery(scraper) }
  )

  private fun scrapeBrewery(scraper: Scraper): List<Item> {
    val brewery = scraper.name
    logger.info("Scraping brewery: ${brewery}")

    return scrapeIndexSafely(scraper, brewery)
      .mapNotNull { scrapeItem(it, brewery) }
      .bestPricedItems()
  }

  private fun scrapeItem(entry: IndexEntry, brewery: String): Item? {
    logger.info("Scraping item: ${entry.rawName}")

    return when (val result = scrapeItemSafely(entry)) {
      is Result.Item -> result.normalise(brewery, entry.url)
      is Result.Skipped -> {
        logger.info("Skipping because: ${result.reason}")
        null
      }
    }
  }

  private fun scrapeIndexSafely(scraper: Scraper, brewery: String): List<IndexEntry> = try {
    scraper.scrapeIndex(jsonGetter.request(scraper.rootUrl))
  } catch (e: Exception) {
    logger.error("Error scraping brewery: ${brewery}", e)
    emptyList()
  }

  private fun scrapeItemSafely(entry: IndexEntry) = try {
    entry.scrapeItem(jsonGetter.request(entry.url))
  } catch (e: Exception) {
    logger.error("Error scraping item: ${entry.rawName}", e)
    Result.Skipped("Scraping error")
  }

  private fun List<Item>.bestPricedItems() = groupBy { it.name to it.summary }
    .map { (key, group) ->
      if (group.size > 1) {
        logger.info("Eliminating ${group.size - 1} item(s) with worse prices for: ${key}")
      }
      group.minBy { it.perItemPrice }!!
    }
}
