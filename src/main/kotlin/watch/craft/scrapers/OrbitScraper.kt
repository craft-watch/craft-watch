package watch.craft.scrapers

import watch.craft.Brewery
import watch.craft.Scraper
import watch.craft.Scraper.Job.Leaf
import watch.craft.Scraper.ScrapedItem
import watch.craft.shopify.extractShopifyOffers
import watch.craft.utils.*
import java.net.URI

class OrbitScraper : Scraper {
  override val brewery = Brewery(
    shortName = "Orbit",
    name = "Orbit Beers",
    location = "Walworth, London",
    websiteUrl = URI("https://www.orbitbeers.com/")
  )

  override val jobs = forPaginatedRootUrl(ROOT_URL) { root ->
    root
      .selectMultipleFrom("#Collection .grid__item")
      .map { el ->
        val title = el.textFrom(".product-card__title")

        Leaf(title, el.hrefFrom("a.grid-view-item__link")) { doc ->
          val desc = doc.formattedTextFrom(".product-single__description")

          // Remove all the dross
          val name = title
            .replace("NEW: ", "")
            .replace("\\S+%".toRegex(), "")   // ABV
            .replace("WLS\\d+".toRegex(), "") // Some weird code
            .split("-")[0]
            .trim()

          ScrapedItem(
            name = name,
            summary = null,
            desc = desc,
            mixed = title.contains("mixed", ignoreCase = true),
            abv = title.maybe { abvFrom() },
            available = ".price--sold-out" !in el,
            offers = doc.orSkip("Can't extract offers, so assume not a beer") {
              extractShopifyOffers(desc.maybe { sizeMlFrom() })
            },
            thumbnailUrl = el.srcFrom("noscript img.grid-view-item__image")
          )
        }
      }
  }

  companion object {
    val ROOT_URL = URI("https://orbit-beers.myshopify.com/collections/all")
  }
}
