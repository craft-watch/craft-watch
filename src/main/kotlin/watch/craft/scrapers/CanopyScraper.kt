package watch.craft.scrapers

import org.jsoup.nodes.Document
import watch.craft.*
import watch.craft.Scraper.IndexEntry
import watch.craft.Scraper.Item
import java.net.URI
import kotlin.text.RegexOption.IGNORE_CASE

class CanopyScraper : Scraper {
  override val name = "Canopy"
  override val rootUrl = URI("https://shop.canopybeer.com/")

  override fun scrapeIndex(root: Document) = root
    .selectMultipleFrom(".grid-uniform")
    .take(3)  // Avoid merch
    .flatMap { it.selectMultipleFrom(".grid__item") }
    .map { el ->
      val a = el.selectFrom(".product__title a")
      val title = el.textFrom(".product__title")

      IndexEntry(title, a.hrefFrom()) { doc ->
        val parts = a.extractFrom(regex = "([^\\d]+) (\\d+(\\.\\d+)?)?")

        if (title.contains("box|pack".toRegex(IGNORE_CASE))) {
          throw SkipItemException("Can't extract number of cans for packs")
        }

        Item(
          thumbnailUrl = el.srcFrom(".grid__image img"),
          name = parts[1],
          summary = null,
          desc = doc.maybeWholeTextFrom(".product-description"),
          available = !(el.text().contains("Sold out", ignoreCase = true)),
          sizeMl = doc.extractFrom(regex = "(\\d+)ml")[1].toInt(),
          abv = if (parts[2].isBlank()) null else parts[2].toDouble(),
          perItemPrice = el.extractFrom(regex = "£(\\d+\\.\\d+)")[1].toDouble()
        )
      }
    }
}
