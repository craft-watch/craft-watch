package watch.craft.scrapers

import watch.craft.Scraper
import watch.craft.Scraper.Job.Leaf
import watch.craft.Scraper.ScrapedItem
import watch.craft.shopify.extractShopifyOffers
import watch.craft.utils.*
import java.net.URI

class OrbitScraper : Scraper {
  override val jobs = forPaginatedRootUrl(ROOT_URL) { root ->
    root
      .selectMultipleFrom(".product-card")
      .map { el ->
        val title = el.textFrom(".product-card__name")

        Leaf(title, el.urlFrom()) { doc ->
          val desc = doc.formattedTextFrom(".product-single__description")

          // Remove all the dross
          val name = title
            .remove(
              "NEW: ",
              "\\S+%",   // ABV
              "WLS\\d+"  // Some weird code
            ).split("-")[0].trim()

          ScrapedItem(
            name = name,
            summary = null,
            desc = desc,
            mixed = title.containsMatch("mixed"),
            abv = title.maybe { abvFrom() },
            available = ".product-card__availability" !in el,
            offers = doc.orSkip("Can't extract offers, so assume not a beer") {
              extractShopifyOffers(desc.maybe { sizeMlFrom() })
            },
            thumbnailUrl = el.urlFrom("noscript img.product-card__image")
          )
        }
      }
  }

  companion object {
    private val ROOT_URL = URI("https://orbitbeers.shop/collections/all")
  }
}
