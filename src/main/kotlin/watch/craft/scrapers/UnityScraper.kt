package watch.craft.scrapers

import watch.craft.Offer
import watch.craft.Scraper
import watch.craft.Scraper.Job.Leaf
import watch.craft.Scraper.ScrapedItem
import watch.craft.shopify.shopifyItems
import watch.craft.utils.*
import java.net.URI

class UnityScraper : Scraper {
  override val jobs = forRootUrls(ROOT_URL) { root ->
    root
      .shopifyItems()
      .map { details ->

        Leaf(details.title, details.url) { doc ->
          val desc = doc.formattedTextFrom(".product-single__description")

          ScrapedItem(
            name = details.title,
            summary = null,
            desc = desc,
            mixed = false,
            abv = desc.extractAbv(),
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

  private fun String.extractAbv() = this
    .split("\n")
    .mapNotNull { it.maybe { abvFrom() } }
    .min()

  companion object {
    private val ROOT_URL = URI("https://unitybrewingco.com/collections/unity-beer")
  }
}
