package choliver.neapi

import choliver.neapi.model.Inventory
import choliver.neapi.model.Item
import choliver.neapi.scrapers.*

class Executor(private val getter: HttpGetter) {
  fun scrapeAll() = Inventory(
    items = SCRAPERS.flatMap { scraper ->
      with(scraper) {
        RealScraperContext(getter).scrape()
          .map {
            Item(
              brewery = scraper.name,
              name = it.name,
              // TODO - validate sane size
              sizeMl = it.sizeMl,
              // TODO - validate sane range
              abv = it.abv?.toFloat(),
              // TODO - validate sane price
              pricePerCan = it.pricePerCan.toFloat(),
              available = it.available,
              // TODO - validate these are absolute URLs
              thumbnailUrl = it.thumbnailUrl?.toString(),
              url = it.url.toString()
            )
          }
      }
    }
  )

  companion object {
    private val SCRAPERS = listOf(
      BoxcarScraper(),
      GipsyHillScraper(),
      HowlingHopsScraper(),
      PressureDropScraper(),
      VillagesScraper()
    )
  }
}
