package choliver.neapi

import choliver.neapi.model.Inventory
import choliver.neapi.model.Item
import choliver.neapi.scrapers.*

class Executor(private val getter: HttpGetter) {
  fun scrapeAll() = Inventory(
    items = SCRAPERS.flatMap { scraper ->
      with(scraper) {
        RealScraperContext(getter).scrape()
          .map { item ->
            Item(
              brewery = scraper.name
                .trim()
                .validate("Brewery name unexpectedly blank") { it.isNotBlank() },
              name = item.name
                .trim()
                .validate("Item name unexpectedly blank") { it.isNotBlank() },
              summary = item.summary
                ?.trim()
                ?.validate("Summary unexpectedly blank") { it.isNotBlank() },
              // TODO - validate sane size
              sizeMl = item.sizeMl,
              abv = item.abv
                ?.validate("ABV unexpectedly high") { it < MAX_ABV },
              perItemPrice = item.perItemPrice
                .validate("Price per ml unexpectedly high") { (it / (item.sizeMl ?: 330)) < MAX_PRICE_PER_ML },
              available = item.available,
              thumbnailUrl = item.thumbnailUrl
                ?.validate("Not an absolute URL") { it.isAbsolute }
                ?.toString(),
              url = item.url
                .validate("Not an absolute URL") { it.isAbsolute }
                .toString()
            )
          }
      }
    }
  )

  private fun <T> T.validate(message: String, predicate: (T) -> Boolean): T {
    if (!predicate(this)) {
      throw IllegalStateException("Validation failed for value (${this}): ${message}")
    }
    return this
  }

  companion object {
    private val SCRAPERS = listOf(
      BoxcarScraper(),
      FourpureScraper(),
      GipsyHillScraper(),
      HowlingHopsScraper(),
      PressureDropScraper(),
      VillagesScraper()
    )

    private const val MAX_ABV = 14.0
    private const val MAX_PRICE_PER_ML = 8.00 / 440   // A fairly bougie can
  }
}
