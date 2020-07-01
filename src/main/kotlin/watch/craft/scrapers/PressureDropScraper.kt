package watch.craft.scrapers

import org.jsoup.nodes.Document
import watch.craft.*
import watch.craft.Scraper.IndexEntry
import watch.craft.Scraper.Item
import java.net.URI

class PressureDropScraper : Scraper {
  override val name = "Pressure Drop"
  override val rootUrls = listOf(URI("https://pressuredropbrewing.co.uk/collections/beers"))

  override fun scrapeIndex(root: Document) = root
    .selectMultipleFrom(".product-grid-item")
    .map { el ->
      val a = el.selectFrom(".grid__image")
      val rawName = el.textFrom(".f--title")

      IndexEntry(rawName, a.hrefFrom()) { doc ->
        val itemText = doc.text()
        val parts = doc.extractFrom(".product__title", "^(.*?)\\s*(-\\s*(.*?))?$")

        if (parts[1].contains("box", ignoreCase = true)) {
          throw SkipItemException("Don't know how to identify number of cans for boxes")
        }

        Item(
          thumbnailUrl = a.srcFrom("noscript img"),
          name = parts[1],
          summary = parts[3].ifBlank { null },
          desc = doc.maybeWholeTextFrom(".product-description"),
          abv = itemText.maybeExtract("(\\d+(\\.\\d+)?)\\s*%")?.get(1)?.toDouble(),
          sizeMl = itemText.maybeExtract("(\\d+)ml")?.get(1)?.toInt(),
          available = true,
          price = doc.priceFrom(".ProductPrice")
        )
      }
    }
}
