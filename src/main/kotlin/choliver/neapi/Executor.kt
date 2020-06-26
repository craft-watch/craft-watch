package choliver.neapi

import choliver.neapi.Scraper.Result
import mu.KotlinLogging

class Executor(getter: HttpGetter) {
  private val jsonGetter = JsonGetter(getter)
  private val logger = KotlinLogging.logger {}

  fun scrapeAll(vararg scrapers: Scraper) = Inventory(
    items = scrapers.flatMap { scraper ->
      val brewery = scraper.name
      logger.info("Executing scraper for brewery: ${brewery}")

      scraper.scrapeIndex(jsonGetter.request(scraper.rootUrl))
        .mapNotNull { (url, scrapeItem) ->
          when (val result = scrapeItem(jsonGetter.request(url))) {
            is Result.Skipped -> {
              logger.info("Skipping because: ${result.reason}")
              null
            }
            is Result.Item -> result.normalise(brewery, url)
          }
        }
        .bestPricedItems()
    }
  )

  private fun List<Item>.bestPricedItems() = groupBy { it.name to it.summary }
    .map { (key, group) ->
      if (group.size > 1) {
        logger.info("Eliminating ${group.size - 1} item(s) with worse prices for: ${key}")
      }
      group.minBy { it.perItemPrice }!!
    }
}
