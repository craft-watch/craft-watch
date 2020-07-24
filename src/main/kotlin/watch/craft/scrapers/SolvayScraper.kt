package watch.craft.scrapers

import watch.craft.Format.KEG
import watch.craft.Offer
import watch.craft.Scraper
import watch.craft.Scraper.Job.Leaf
import watch.craft.Scraper.ScrapedItem
import watch.craft.dsl.*

class SolvayScraper : Scraper {
  override val jobs = forRoots(*ROOTS) { root ->
    root
      .selectMultipleFrom(".content .grid-item")
      .map { el ->
        val rawName = el.textFrom(".grid-title")

        leaf(rawName, el.urlFrom("a.grid-item-link")) { doc ->
          val nameParts = rawName.extract("(.*?)\\s+\\|\\s+(?:(.*?)\\s+\\d)?")
          val desc = doc.selectFrom(".ProductItem-details-excerpt")
          val mixed = rawName.containsMatch("mix")

          ScrapedItem(
            name = nameParts[1],
            summary = if (mixed) null else nameParts[2],
            desc = desc.formattedTextFrom(),
            mixed = mixed,
            abv = if (mixed) null else rawName.abvFrom(),
            available = true,
            offers = setOf(
              Offer(
                quantity = rawName.maybe { quantityFrom() } ?: 1,
                totalPrice = el.priceFrom(".product-price"),
                format = if (rawName.containsMatch("keg")) KEG else null,
                sizeMl = if (mixed) null else desc.sizeMlFrom()
              )
            ),
            // Request a smaller image
            thumbnailUrl = doc.urlFrom("img.ProductItem-gallery-slides-item-image") { "$it?format=200w" }
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
