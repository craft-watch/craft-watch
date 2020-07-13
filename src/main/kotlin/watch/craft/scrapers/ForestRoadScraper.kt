package watch.craft.scrapers

import watch.craft.Brewery
import watch.craft.Offer
import watch.craft.Scraper
import watch.craft.Scraper.Job.Leaf
import watch.craft.Scraper.ScrapedItem
import watch.craft.SkipItemException
import watch.craft.utils.*
import java.net.URI

class ForestRoadScraper : Scraper {
  override val brewery = Brewery(
    shortName = "Forest Road",
    name = "Forest Road Brewing Co",
    location = "Hackney, London",
    websiteUrl = URI("https://www.forestroad.co.uk/")
  )

  // TODO - specials?  https://www.forestroad.co.uk/shop?category=SPECIAL

  override val jobs = forRootUrls(ROOT_URL) { root ->
    root
      .selectMultipleFrom(".Main--products-list .ProductList-item")
      .map { el ->
        val title = el.textFrom(".ProductList-title")

        Leaf(title, el.hrefFrom("a.ProductList-item-link")) { doc ->
          if (title.contains("subscription", ignoreCase = true)) {
            throw SkipItemException("Subscriptions aren't something we can model")
          }

          val desc = doc.formattedTextFrom(".ProductItem-details-excerpt")

          val mixed = title.contains("mixed", ignoreCase = true)


          ScrapedItem(
            name = title.toTitleCase(),
            summary = null,
            desc = desc,
            mixed = true,
            abv = if (mixed) null else desc.abvFrom(),
            available = true,
            offers = setOf(
              Offer(
                quantity = 1,
                totalPrice = el.priceFrom(".product-price"),
                sizeMl = null
              )
            ),
            thumbnailUrl = el.dataSrcFrom("img.ProductList-image")
          )
        }
      }
  }

  companion object {
    val ROOT_URL = URI("https://www.forestroad.co.uk/shop?category=BEER")
  }
}
