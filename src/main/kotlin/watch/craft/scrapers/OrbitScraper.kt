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
      .selectMultipleFrom("#Collection .grid__item")
      .map { el ->
        val title = el.textFrom(".product-card__title")

        Leaf(title, el.urlFrom("a.grid-view-item__link")) { doc ->
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
            available = ".price--sold-out" !in el,
            offers = doc.orSkip("Can't extract offers, so assume not a beer") {
              extractShopifyOffers(desc.maybe { sizeMlFrom() })
            },
            thumbnailUrl = el.urlFrom("noscript img.grid-view-item__image")
          )
        }
      }
  }

  companion object {
    private val ROOT_URL = URI("https://orbit-beers.myshopify.com/collections/all")
  }
}
