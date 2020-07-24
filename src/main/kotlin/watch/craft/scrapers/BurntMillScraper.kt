package watch.craft.scrapers

import watch.craft.Format.CAN
import watch.craft.Offer
import watch.craft.Scraper
import watch.craft.Scraper.Job.Leaf
import watch.craft.Scraper.ScrapedItem
import watch.craft.dsl.*

class BurntMillScraper : Scraper {
  override val jobs = forRoots(ROOT) { root ->
    root
      .selectMultipleFrom(".ProductItem")
      .map { el ->
        val title = el.textFrom(".ProductItem__Title")
        Leaf(title, el.urlFrom(".ProductItem__ImageWrapper")) { doc ->
          ScrapedItem(
            name = title
              .cleanse(
                "\\d+ml",
                "\\S+%",
                "\\d+ pack\\s+-\\s+",
                "\\s+-\\s+.*"
              )
              .toTitleCase(),
            summary = null,
            desc = doc.formattedTextFrom(".ProductMeta__Description"),
            abv = title.abvFrom(),
            available = el.maybe { textFrom(".ProductItem__Label") } != "Sold out",
            offers = setOf(
              Offer(
                quantity = title.maybe { quantityFrom() } ?: 1,
                totalPrice = el.priceFrom(".ProductItem__Price"),
                sizeMl = title.maybe { sizeMlFrom() },
                format = STANDARD_FORMAT
              )
            ),
            thumbnailUrl = el.urlFrom(".ProductItem__Image")
          )
        }
      }
  }

  companion object {
    private val ROOT = root("https://burnt-mill-brewery.myshopify.com/collections/frontpage")

    private val STANDARD_FORMAT = CAN
  }
}
