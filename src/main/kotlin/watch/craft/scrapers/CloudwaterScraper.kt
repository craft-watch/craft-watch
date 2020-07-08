package watch.craft.scrapers

import watch.craft.Brewery
import watch.craft.Scraper
import watch.craft.Scraper.Job.Leaf
import watch.craft.Scraper.ScrapedItem
import watch.craft.utils.*
import java.net.URI

class CloudwaterScraper : Scraper {
  override val brewery = Brewery(
    shortName = "Cloudwater",
    name = "Cloudwater Brew Co",
    location = "Manchester",
    websiteUrl = URI("https://cloudwaterbrew.co/")
  )

  override val jobs = forRootUrls(ROOT_URL) { root ->
    root
      .selectMultipleFrom(".product-collection .grid-item")
      .map { el ->
        val a = el.selectFrom(".product-title")
        val rawName = a.textFrom()

        Leaf(rawName, a.hrefFrom()) { doc ->
          val desc = el.selectFrom(".short-description")

          ScrapedItem(
            name = rawName,
            summary = null,
            desc = null,
            sizeMl = desc.sizeMlFrom(),
            abv = desc.abvFrom(),
            available = true,
            numItems = 1,
            price = el.priceFrom(".price-regular"),
            thumbnailUrl = el.dataSrcFrom(".product-grid-image img")
          )
        }
      }
  }

  companion object {
    val ROOT_URL = URI("https://shop.cloudwaterbrew.co/collections/cloudwater-beer")
  }
}
