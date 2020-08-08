package watch.craft.scrapers

import watch.craft.Format.KEG
import watch.craft.Offer
import watch.craft.Scraper

import watch.craft.Scraper.Node.ScrapedItem
import watch.craft.dsl.*
import kotlin.math.max

class SolvayScraper : Scraper {
  override val roots = fromHtmlRoots(*ROOTS) { root ->
    root()
      .selectMultipleFrom(".content .grid-item")
      .map { el ->
        val rawName = el.textFrom(".grid-title")

        fromHtml(rawName, el.urlFrom("a.grid-item-link")) { doc ->
          val nameParts = rawName.split(" | ")
          val desc = doc().selectFrom(".ProductItem-details-excerpt").formattedTextFrom()
          val descQuantities = desc.collectFromLines { quantityFrom() }
          val mixed = rawName.containsMatch("mix") || descQuantities.size > 1

          ScrapedItem(
            name = nameParts[0],
            summary = if (nameParts.size > 1) nameParts[1].cleanse("\\S+%") else null,
            desc = desc,
            mixed = mixed,
            abv = if (mixed) null else desc.abvFrom(),
            available = true,
            offers = setOf(
              Offer(
                quantity = rawName.maybe { quantityFrom() } ?: max(descQuantities.sum(), 1),
                totalPrice = el.priceFrom(".product-price"),
                format = if (rawName.containsMatch("keg")) KEG else null,
                sizeMl = if (mixed) null else desc.sizeMlFrom()
              )
            ),
            // Request a smaller image
            thumbnailUrl = doc().urlFrom("img.ProductItem-gallery-slides-item-image") { "$it?format=200w" }
          )
        }
      }
  }

  companion object {
    private val ROOTS = arrayOf(
      root("https://www.solvaysociety.com/shop?category=Core+Range"),
      root("https://www.solvaysociety.com/shop?category=Specials")
    )
  }
}
