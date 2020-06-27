package choliver.neapi.scrapers

import choliver.neapi.*
import choliver.neapi.Scraper.IndexEntry
import choliver.neapi.Scraper.Result.Item
import choliver.neapi.Scraper.Result.Skipped
import org.jsoup.nodes.Document
import java.net.URI

class PressureDropScraper : Scraper {
  override val name = "Pressure Drop"
  override val rootUrl = URI("https://pressuredropbrewing.co.uk/collections/beers")

  override fun scrapeIndex(root: Document) = root
    .selectMultipleFrom(".product-grid-item")
    .map { el ->
      val a = el.selectFrom(".grid__image")
      val rawName = el.textFrom(".f--title")

      IndexEntry(rawName, a.hrefFrom()) { doc ->
        val itemText = doc.text()
        val parts = doc.extractFrom(".product__title", "^(.*?)\\s*(-\\s*(.*?))?$")

        if (parts[1].contains("box", ignoreCase = true)) {
          Skipped("Don't know how to identify number of cans for boxes")
        } else {
          Item(
            thumbnailUrl = a.srcFrom("noscript img"),
            name = parts[1],
            summary = parts[3].ifBlank { null },
            abv = itemText.maybeExtract("(\\d+(\\.\\d+)?)\\s*%")?.get(1)?.toDouble(),
            sizeMl = itemText.maybeExtract("(\\d+)ml")?.get(1)?.toInt(),
            available = true,
            perItemPrice = doc.priceFrom(".ProductPrice")
          )
        }
      }
    }
}
