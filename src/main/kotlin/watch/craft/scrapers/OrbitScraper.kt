package watch.craft.scrapers

import watch.craft.Scraper
import watch.craft.Scraper.Job.Leaf
import watch.craft.Scraper.ScrapedItem
import watch.craft.dsl.*
import watch.craft.shopify.extractShopifyOffers

class OrbitScraper : Scraper {
  override val jobs = forPaginatedRoots(ROOT) { root ->
    root
      .selectMultipleFrom(".product-card")
      .map { el ->
        val title = el.textFrom(".product-card__name")

        Leaf(title, el.urlFrom()) { doc ->
          val desc = doc.formattedTextFrom(".product-single__description")

          // Remove all the dross
          val name = title
            .cleanse(
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
    private val ROOT = root("https://orbitbeers.shop/collections/all")
  }
}
