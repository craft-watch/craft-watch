package choliver.neapi.scrapers

import choliver.neapi.ParsedItem
import choliver.neapi.Scraper
import choliver.neapi.Scraper.Context
import java.net.URI

class BoxcarScraper : Scraper {
  override val name = "Boxcar"

  override fun Context.scrape() = request(ROOT_URL)
    .shopifyItems()
    .map { details ->
      val parts = details.title.extract("^(.*?) // (.*?)% *(.*?)? // (.*?)ml$")!!

      ParsedItem(
        thumbnailUrl = details.thumbnailUrl,
        url = details.url,
        name = parts[1],
        abv = parts[2].toBigDecimal(),
        summary = parts[3].ifBlank { null },
        sizeMl = parts[4].toInt(),
        available = details.available,
        pricePerCan = details.price
      )
    }

  companion object {
    private val ROOT_URL = URI("https://shop.boxcarbrewery.co.uk/collections/beer")
  }
}
