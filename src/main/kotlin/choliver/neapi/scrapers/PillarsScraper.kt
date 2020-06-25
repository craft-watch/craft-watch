package choliver.neapi.scrapers

import choliver.neapi.ParsedItem
import choliver.neapi.Scraper
import choliver.neapi.Scraper.Context
import java.net.URI

class PillarsScraper : Scraper {
  override val name = "Pillars"

  override fun Context.scrape() = request(ROOT_URL)
    .shopifyItems()
    .map { details ->
//      val itemText = request(details.url).text()
//      val sizeMl = itemText.extract("(\\d+)ml")?.get(1)?.toInt()

      ParsedItem(
        thumbnailUrl = details.thumbnailUrl,
        url = details.url,
        name = details.title,
        summary = null,
        sizeMl = null,
        available = details.available,
        perItemPrice = details.price
      )
    }

  companion object {
    private val ROOT_URL = URI("https://shop.pillarsbrewery.com/collections/pillars-beers")
  }
}
