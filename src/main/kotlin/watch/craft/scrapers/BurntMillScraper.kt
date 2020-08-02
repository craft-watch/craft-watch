package watch.craft.scrapers

import watch.craft.Format.CAN
import watch.craft.Offer
import watch.craft.Scraper

import watch.craft.Scraper.Node.ScrapedItem
import watch.craft.dsl.*

class BurntMillScraper : Scraper {
  override val roots = fromHtmlRoots(ROOT) { root ->
    root()
      .selectMultipleFrom(".ProductItem")
      .map { el ->
        val title = el.textFrom(".ProductItem__Title")
        fromHtml(title, el.urlFrom(".ProductItem__ImageWrapper")) { doc ->
          val desc = doc().formattedTextFrom(".ProductMeta__Description")
          val quantities = desc.collectFromLines { quantityFrom() }
          val mixed = quantities.size > 1

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
            desc = desc,
            mixed = mixed,
            abv = if (mixed) null else title.abvFrom(),
            available = el.maybe { textFrom(".ProductItem__Label") } != "Sold out",
            offers = setOf(
              Offer(
                quantity = if (mixed) quantities.sum() else title.maybe { quantityFrom() } ?: 1,
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
