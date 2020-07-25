package watch.craft.scrapers

import watch.craft.Offer
import watch.craft.Scraper
import watch.craft.Scraper.Node.ScrapedItem
import watch.craft.dsl.*
import watch.craft.shopify.shopifyItems

class BoxcarScraper : Scraper {
  override val roots = fromHtmlRoots(ROOT) { root ->
    root
      .shopifyItems()
      .map { details ->
        fromHtml(details.title, details.url) { doc ->
          val parts = details.title.extract("^(.*?) // (.*?)% *(.*?)? //")

          ScrapedItem(
            thumbnailUrl = details.thumbnailUrl,
            name = parts[1],
            abv = parts[2].toDouble(),
            summary = parts[3].ifBlank { null },
            desc = doc.maybe { formattedTextFrom(".product-single__description") }
              ?.cleanse("^DESCRIPTION"),
            available = details.available,
            offers = setOf(
              Offer(
                totalPrice = details.price,
                sizeMl = details.title.sizeMlFrom()
              )
            )
          )
        }
      }
  }

  companion object {
    private val ROOT = root("https://shop.boxcarbrewery.co.uk/collections/beer")
  }
}
