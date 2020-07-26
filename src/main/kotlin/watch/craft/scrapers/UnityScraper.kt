package watch.craft.scrapers

import watch.craft.Offer
import watch.craft.Scraper

import watch.craft.Scraper.Node.ScrapedItem
import watch.craft.dsl.*
import watch.craft.shopify.shopifyItems

class UnityScraper : Scraper {
  override val roots = fromHtmlRoots(ROOT) { root ->
    root()
      .shopifyItems()
      .map { details ->

        fromHtml(details.title, details.url) { doc ->
          val desc = doc().formattedTextFrom(".product-single__description")

          ScrapedItem(
            name = details.title,
            summary = null,
            desc = desc,
            mixed = false,
            abv = desc.collectFromLines { abvFrom() }.min(),
            available = details.available,
            offers = setOf(
              Offer(
                totalPrice = details.price,
                sizeMl = desc.sizeMlFrom()
              )
            ),
            thumbnailUrl = details.thumbnailUrl
          )
        }
      }
  }

  companion object {
    private val ROOT = root("https://unitybrewingco.com/collections/unity-beer")
  }
}
