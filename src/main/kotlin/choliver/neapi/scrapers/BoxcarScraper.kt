package choliver.neapi.scrapers

import choliver.neapi.Scraper
import choliver.neapi.Scraper.IndexEntry
import choliver.neapi.Scraper.Result.Item
import choliver.neapi.extract
import choliver.neapi.shopifyItems
import org.jsoup.nodes.Document
import java.net.URI

class BoxcarScraper : Scraper {
  override val name = "Boxcar"
  override val rootUrl = URI("https://shop.boxcarbrewery.co.uk/collections/beer")

  override fun scrapeIndex(root: Document) = root
    .shopifyItems()
    .map { details ->
      IndexEntry(details.title, details.url) {
        val parts = details.title.extract("^(.*?) // (.*?)% *(.*?)? // (.*?)ml$")!!

        Item(
          thumbnailUrl = details.thumbnailUrl,
          name = parts[1],
          abv = parts[2].toDouble(),
          summary = parts[3].ifBlank { null },
          sizeMl = parts[4].toInt(),
          available = details.available,
          perItemPrice = details.price
        )
      }
    }
}
