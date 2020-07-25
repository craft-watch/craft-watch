package watch.craft.scrapers

import watch.craft.Scraper

import watch.craft.Scraper.Node.ScrapedItem
import watch.craft.SkipItemException
import watch.craft.dsl.*
import watch.craft.shopify.extractShopifyOffers

class RedchurchScraper : Scraper {
  override val root = forRoots(ROOT) { root ->
    root
      .selectMultipleFrom(".product")
      .map { el ->
        val title = el.selectFrom(".product__title")
        val rawName = title.text()

        work(rawName, title.urlFrom("a")) { doc ->
          val nameParts = rawName.extract(regex = "(Mixed Case - )?(.*)")
          val mixed = !nameParts[1].isBlank()
          val sizeMl = doc.maybe { sizeMlFrom() }
          val abv = doc.maybe { abvFrom() }

          if (!mixed && sizeMl == null && abv == null) {
            throw SkipItemException("Can't identify ABV or size for non-mixed case, so assume it's not a beer")
          }

          ScrapedItem(
            thumbnailUrl = doc.urlFrom(".product-single__photo"),
            name = nameParts[2],
            desc = doc.maybe { formattedTextFrom(".product-single__description") },
            mixed = mixed,
            abv = abv,
            available = ".sold-out-text" !in el,
            offers = doc.orSkip("Don't know how to identify number of items") {
              extractShopifyOffers(sizeMl)
            }
          )
        }
      }
  }

  companion object {
    private val ROOT = root("https://redchurch.store/")
  }
}
