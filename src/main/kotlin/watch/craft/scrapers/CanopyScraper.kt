package watch.craft.scrapers

import watch.craft.Offer
import watch.craft.Scraper
import watch.craft.Scraper.Job.Leaf
import watch.craft.Scraper.ScrapedItem
import watch.craft.SkipItemException
import watch.craft.utils.*
import java.net.URI
import kotlin.text.RegexOption.IGNORE_CASE

class CanopyScraper : Scraper {
  override val jobs = forRootUrls(ROOT_URL) { root ->
    root
      .selectMultipleFrom(".grid-uniform")
      .take(3)  // Avoid merch
      .flatMap { it.selectMultipleFrom(".grid__item") }
      .map { el ->
        val a = el.selectFrom(".product__title a")
        val title = el.textFrom(".product__title")

        Leaf(title, a.urlFrom()) { doc ->
          val parts = a.extractFrom(regex = "([^\\d]+) (\\d+(\\.\\d+)?)?")

          if (title.contains("box|pack".toRegex(IGNORE_CASE))) {
            throw SkipItemException("Can't extract number of cans for packs")
          }

          ScrapedItem(
            thumbnailUrl = el.urlFrom(".grid__image img"),
            name = parts[1],
            summary = null,
            desc = doc.maybe { formattedTextFrom(".product-description") },
            available = !(el.text().contains("Sold out", ignoreCase = true)),
            abv = if (parts[2].isBlank()) null else parts[2].toDouble(),
            offers = setOf(
              Offer(
                totalPrice = el.extractFrom(regex = "£(\\d+\\.\\d+)")[1].toDouble(),
                sizeMl = doc.sizeMlFrom()
              )
            )
          )
        }
      }
  }

  companion object {
    private val ROOT_URL = URI("https://shop.canopybeer.com/")
  }
}
