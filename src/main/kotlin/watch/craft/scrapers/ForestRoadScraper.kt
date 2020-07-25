package watch.craft.scrapers

import watch.craft.Format.BOTTLE
import watch.craft.Offer
import watch.craft.Scraper

import watch.craft.Scraper.Node.ScrapedItem
import watch.craft.SkipItemException
import watch.craft.dsl.*
import kotlin.math.max

class ForestRoadScraper : Scraper {
  override val root = forRoots(*ROOTS) { root ->
    root
      .selectMultipleFrom(".Main--products-list .ProductList-item")
      .map { el ->
        val title = el.textFrom(".ProductList-title")

        work(title, el.urlFrom("a.ProductList-item-link")) { doc ->
          if (title.containsMatch("subscription")) {
            throw SkipItemException("Subscriptions aren't something we can model")
          }

          val desc = doc.formattedTextFrom(".ProductItem-details-excerpt").toTitleCase()
          val descLines = desc.split("\n")
          val mixed = title.containsMatch("mixed")

          ScrapedItem(
            name = title.cleanse("[(].*[)]", "cans").toTitleCase(),
            summary = if (descLines[0].containsMatch("@")) null else descLines[0], // Filter out nonsense
            desc = desc,
            mixed = mixed,
            abv = if (mixed) null else desc.orSkip("No ABV, so assume not a beer") { abvFrom() },
            available = true,
            offers = setOf(
              Offer(
                quantity = title.maybe { quantityFrom() }
                  ?: max(1, desc.collectFromLines { quantityFrom() }.sum()),
                totalPrice = el.priceFrom(".product-price"),
                sizeMl = title.maybe { sizeMlFrom() } ?: desc.maybe { sizeMlFrom() },
                format = title.formatFrom() ?: BOTTLE
              )
            ),
            thumbnailUrl = el.urlFrom("img.ProductList-image")
          )
        }
      }
  }

  companion object {
    private val ROOTS = arrayOf(
      root("https://www.forestroad.co.uk/shop?category=BEER"),
      root("https://www.forestroad.co.uk/shop?category=SPECIAL")
    )
  }
}
