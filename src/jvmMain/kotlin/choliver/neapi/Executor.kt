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
              brewery = scraper.name,
              name = item.name,
              // TODO - validate sane size
              sizeMl = item.sizeMl,
              // TODO - validate sane range
              abv = item.abv?.toFloat(),
              // TODO - validate sane price
              pricePerCan = item.pricePerCan.toFloat()
                .validate("Price unexpectedly high") { it < MAX_PRICE_PER_CAN },
              available = item.available,
              // TODO - validate these are absolute URLs
              thumbnailUrl = item.thumbnailUrl?.toString(),
              url = item.url.toString()
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
      GipsyHillScraper(),
      HowlingHopsScraper(),
      PressureDropScraper(),
      VillagesScraper()
    )

    private const val MAX_PRICE_PER_CAN = 8.00
  }
}
