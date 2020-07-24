package watch.craft.scrapers

import watch.craft.Offer
import watch.craft.Scraper
import watch.craft.Scraper.Job.Leaf
import watch.craft.Scraper.ScrapedItem
import watch.craft.SkipItemException
import watch.craft.dsl.*

class PressureDropScraper : Scraper {
  override val jobs = forRoots(ROOT) { root ->
    root
      .selectMultipleFrom(".product-grid-item")
      .map { el ->
        val a = el.selectFrom(".grid__image")
        val rawName = el.textFrom(".f--title")

        Leaf(rawName, a.urlFrom()) { doc ->
          val itemText = doc.text()
          val parts = doc.extractFrom(".product__title", "^(.*?)\\s*(-\\s*(.*?))?$")

          if (parts[1].containsMatch("box")) {
            throw SkipItemException("Don't know how to identify number of cans for boxes")
          }

          ScrapedItem(
            thumbnailUrl = a.urlFrom("noscript img"),
            name = parts[1],
            summary = parts[3].ifBlank { null },
            desc = doc.maybe { formattedTextFrom(".product-description") },
            abv = itemText.maybe { abvFrom() },
            available = true,
            offers = setOf(
              Offer(
                totalPrice = doc.priceFrom(".ProductPrice"),
                sizeMl = itemText.maybe { sizeMlFrom() }
              )
            )
          )
        }
      }
  }

  companion object {
    private val ROOT = root("https://pressuredropbrewing.co.uk/collections/beers")
  }
}
