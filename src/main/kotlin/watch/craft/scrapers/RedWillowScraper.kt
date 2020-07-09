package watch.craft.scrapers

import watch.craft.Brewery
import watch.craft.Scraper
import watch.craft.Scraper.Job.Leaf
import watch.craft.Scraper.ScrapedItem
import watch.craft.utils.*
import java.net.URI

class RedWillowScraper : Scraper {
  override val brewery = Brewery(
    shortName = "Red Willow",
    name = "Red Willow Brewery",
    location = "Macclesfield, Cheshire",
    websiteUrl = URI("https://www.redwillowbrewery.com/")
  )

  override val jobs = forRootUrls(ROOT_URL) { root ->
    root
      .selectMultipleFrom(".ProductList-grid .ProductList-item")
      .map { el ->
        val rawName = el.textFrom(".ProductList-title")

        Leaf(rawName, el.hrefFrom("a.ProductList-item-link")) { doc ->

          val parts = rawName.extract("([^0-9]+)\\s+(\\d(\\.\\d+)?)%")//\\s(.*?)")
          println(parts)

          ScrapedItem(
            name = rawName,
            summary = null,
            desc = doc.formattedTextFrom(".ProductItem-details-excerpt"),
            sizeMl = null,
            abv = null,
            available = true,
            numItems = 1,
            price = 0.00,
            thumbnailUrl = el.dataSrcFrom("img.ProductList-image")
          )
        }
      }
  }

  companion object {
    private val ROOT_URL = URI("https://www.redwillowbrewery.com/shop")
  }

}
