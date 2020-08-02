package watch.craft.scrapers

import watch.craft.Offer
import watch.craft.Scraper
import watch.craft.Scraper.Node.ScrapedItem
import watch.craft.dsl.*

// TODO - ensure framework resolves items case-insensitively

class MadSquirrelScraper : Scraper {
  override val failOn404 = false  // Seems to be a server config issue

  override val roots = fromPaginatedRoots(*ROOTS) { root ->
    root()
      .selectMultipleFrom(".itemSmall")
      .map { el ->
        val title = el.textFrom(".itemTitle")
        fromHtml(title, el.urlFrom(".itemImageWrap a")) { doc ->
          ScrapedItem(
            name = title.cleanse("\\s+-\\s+.*", "[(].*[)]"),
            summary = el.textFrom(".itemStyle"),
            desc = doc().formattedTextFrom(".itemSummary").ifBlank { null },
            abv = null,
            available = true,
            offers = setOf(
              Offer(
                quantity = 1,
                totalPrice = el.priceFrom(".priceStandard"),
                sizeMl = doc().maybe { sizeMlFrom(".sizeName") },
                format = null
              )
            ),
            thumbnailUrl = el.urlFrom(".imageInnerWrap img")
          )
        }
      }
  }

  companion object {
    private val ROOTS = arrayOf(
      root("https://www.madsquirrelbrew.co.uk/browse/c-beer-7/")
    )
  }
}
