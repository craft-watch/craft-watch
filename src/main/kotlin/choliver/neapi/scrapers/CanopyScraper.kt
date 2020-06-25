package choliver.neapi.scrapers

import choliver.neapi.*
import choliver.neapi.Scraper.IndexEntry
import choliver.neapi.Scraper.Result.Item
import org.jsoup.nodes.Document
import java.net.URI
import kotlin.text.RegexOption.IGNORE_CASE

class CanopyScraper : Scraper {
  override val name = "Canopy"
  override val rootUrl = URI("https://shop.canopybeer.com/")

  override fun scrapeIndex(root: Document) = root
    .selectMultipleFrom(".grid-uniform")
    .take(3)  // Avoid merch
    .flatMap { it.selectMultipleFrom(".grid__item") }
    .filterNot { it.textFrom(".product__title").contains("box|pack".toRegex(IGNORE_CASE)) }  // Don't know how to extract number of cans
    .map { el ->
      val a = el.selectFrom(".product__title a")

      IndexEntry(a.hrefFrom()) { doc ->
        val parts = a.extractFrom(regex = "([^\\d]+) (\\d+(\\.\\d+)?)?")!!

        Item(
          thumbnailUrl = el.srcFrom(".grid__image img"),
          name = parts[1],
          summary = null,
          available = !(el.text().contains("Sold out", ignoreCase = true)),
          sizeMl = doc.extractFrom(regex = "(\\d+)ml")!![1].toInt(),
          abv = if (parts[2].isBlank()) null else parts[2].toDouble(),
          perItemPrice = el.extractFrom(regex = "£(\\d+\\.\\d+)")!![1].toDouble()
        )
      }
    }
}
