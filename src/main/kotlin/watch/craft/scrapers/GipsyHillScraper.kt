package watch.craft.scrapers

import watch.craft.Offer
import watch.craft.Scraper

import watch.craft.Scraper.Node.ScrapedItem
import watch.craft.dsl.*

class GipsyHillScraper : Scraper {
  override val roots = fromHtmlRoots(*ROOTS) { root ->
    root()
      .selectMultipleFrom(".ProductCard")
      .map { el ->
        val title = el.textFrom(".ProductCard__title")

        fromHtml(title, el.urlFrom(".ProductCard__link")) { doc ->
          ScrapedItem(
            name = title,
            summary = el.textFrom(".ProductCard__style"),
            desc = doc().maybe { formattedTextFrom(".PDPInformation__content") },
            mixed = false,
            available = !el.containsMatchFrom(".Button", "sold out"),
            abv = el.abvFrom(".ProductCard__abv"),
            offers = setOf(
              Offer(
                quantity = 1,
                totalPrice = doc().priceFrom(".ProductPrice"),
                sizeMl = doc().sizeMlFrom(".PackSizes__list"),
                format = doc().formatFrom(".PDPInformation__table", fullProse = false)
              )
            ),
            thumbnailUrl = el.urlFrom("img.Image")
          )
        }
      }
  }

  companion object {
    // We don't bother with multi-packs, because the prices are no better
    private val ROOTS = arrayOf(
      root("https://gipsyhillbrew.com/products/core-beers"),
      root("https://gipsyhillbrew.com/products/specials")
    )
  }
}
