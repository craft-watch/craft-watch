package choliver.neapi.scrapers

import choliver.neapi.ScrapedItem
import choliver.neapi.Scraper
import choliver.neapi.Scraper.Context
import choliver.neapi.extract
import choliver.neapi.shopifyItems
import java.net.URI

class BoxcarScraper : Scraper {
  override val name = "Boxcar"

  override fun Context.scrape() = request(ROOT_URL)
    .shopifyItems()
    .map { details ->
      val parts = details.title.extract("^(.*?) // (.*?)% *(.*?)? // (.*?)ml$")!!

      ScrapedItem(
        thumbnailUrl = details.thumbnailUrl,
        url = details.url,
        name = parts[1],
        abv = parts[2].toDouble(),
        summary = parts[3].ifBlank { null },
        sizeMl = parts[4].toInt(),
        available = details.available,
        perItemPrice = details.price
      )
    }

  companion object {
    private val ROOT_URL = URI("https://shop.boxcarbrewery.co.uk/collections/beer")
  }
}
