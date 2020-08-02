package watch.craft.scrapers

import watch.craft.Offer
import watch.craft.Scraper

import watch.craft.Scraper.Node.ScrapedItem
import watch.craft.SkipItemException
import watch.craft.dsl.*

class CanopyScraper : Scraper {
  override val roots = fromHtmlRoots(ROOT) { root ->
    root()
      .selectMultipleFrom(".grid-uniform")
      .take(3)  // Avoid merch
      .flatMap { it.selectMultipleFrom(".grid__item") }
      .map { el ->
        val a = el.selectFrom(".product__title a")
        val title = el.textFrom(".product__title")

        fromHtml(title, a.urlFrom()) { doc ->
          val parts = a.extractFrom(regex = "([^\\d]+) (\\d+(\\.\\d+)?)?")

          if (title.containsWord(*BLACKLIST.toTypedArray())) {
            throw SkipItemException("Can't extract number of cans")
          }

          ScrapedItem(
            thumbnailUrl = el.urlFrom(".grid__image img"),
            name = parts[1],
            summary = null,
            desc = doc().maybe { formattedTextFrom(".product-description") },
            available = !(el.text().containsMatch("sold out")),
            abv = if (parts[2].isBlank()) null else parts[2].toDouble(),
            offers = setOf(
              Offer(
                totalPrice = el.extractFrom(regex = "£(\\d+\\.\\d+)")[1].toDouble(),
                sizeMl = doc().sizeMlFrom()
              )
            )
          )
        }
      }
  }

  companion object {
    private val ROOT = root("https://shop.canopybeer.com/")

    private val BLACKLIST = listOf("box", "pack", "club")
  }
}
