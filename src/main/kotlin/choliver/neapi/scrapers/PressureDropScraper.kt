package choliver.neapi.scrapers

import choliver.neapi.*
import choliver.neapi.Scraper.IndexEntry
import org.jsoup.nodes.Document
import java.net.URI

class PressureDropScraper : Scraper {
  override val name = "Pressure Drop"
  override val rootUrl = URI("https://pressuredropbrewing.co.uk/collections/beers")

  override fun scrapeIndex(root: Document) = root
    .selectMultipleFrom(".product-grid-item")
    .map { el ->
      val a = el.selectFrom(".grid__image")

      IndexEntry(a.hrefFrom()) { doc ->
        val itemText = doc.text()
        val parts = doc.extractFrom(".product__title", "^(.*?)\\s*-\\s*(.*?)$")!!

        ScrapedItem(
          thumbnailUrl = a.srcFrom("noscript img"),
          name = parts[1],
          summary = parts[2],
          abv = itemText.extract("(\\d+(\\.\\d+)?)\\s*%")?.get(1)?.toDouble(),  // TODO - deal with all the ?
          sizeMl = itemText.extract("(\\d+)ml")?.get(1)?.toInt(),
          available = true,
          perItemPrice = doc.priceFrom(".ProductPrice")
        )
      }
    }
}
