package choliver.neapi

import org.jsoup.Jsoup
import java.net.URI

class Main(
  private val getUrl: (URI) -> String
) {
  private val scrapers = listOf(
    BoxcarScraper(),
    GipsyHillScraper(),
    HowlingHopsScraper(),
    VillagesScraper()
  )

  fun scrapeAll() = scrapers.flatMap { scraper ->
    scraper.scrape(Jsoup.parse(getUrl(scraper.rootUrl)))
      .map {
        Item(
          brewery = scraper.name,
          name = it.name,
          price = it.price,
          available = it.available,
          url = it.url
        )
      }
  }
}
