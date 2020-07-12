package watch.craft.scrapers

import watch.craft.Brewery
import watch.craft.Offer
import watch.craft.Scraper
import watch.craft.Scraper.Job.Leaf
import watch.craft.Scraper.ScrapedItem
import watch.craft.utils.*
import java.net.URI

class OrbitScraper : Scraper {
  override val brewery = Brewery(
    shortName = "Orbit",
    name = "Orbit Beers",
    location = "Walworth, London",
    websiteUrl = URI("https://www.orbitbeers.com/")
  )

  // TODO - pagination

  override val jobs = forRootUrls(ROOT_URL) { root ->
    root
      .selectMultipleFrom("#Collection .grid__item")
      .map { el ->
        val title = el.textFrom(".product-card__title")

        Leaf(title, el.hrefFrom("a.grid-view-item__link")) { doc ->
          ScrapedItem(
            name = title,
            summary = null,
            desc = doc.formattedTextFrom(".product-single__description"),
            mixed = false,    // TODO
            abv = title.maybe { abvFrom() },
            available = ".price--sold-out" !in el,
            offers = setOf(
              Offer(
                quantity = 1,
                totalPrice = el.priceFrom(".price"),    // TODO - probably needs to be smarter
                sizeMl = null
              )
            ),
            thumbnailUrl = el.srcFrom("noscript img.grid-view-item__image")
          )
        }
      }
  }

  companion object {
    val ROOT_URL = URI("https://orbit-beers.myshopify.com/collections/all")
  }
}
