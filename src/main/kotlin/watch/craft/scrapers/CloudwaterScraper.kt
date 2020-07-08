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

  // TODO - mixed
  // TODO - number of items
  // TODO - eliminate size from summary

  override val jobs = forRootUrls(ROOT_URL) { root ->
    root
      .selectMultipleFrom(".product-collection .grid-item")
      .map { el ->
        val a = el.selectFrom("a.product-title")
        val nameLines = a.formattedTextFrom().split("\n")

        Leaf(nameLines[0], a.hrefFrom()) { doc ->
          val desc = doc.selectFrom(".short-description")

          ScrapedItem(
            name = nameLines[0],
            summary = if (nameLines.size > 1) nameLines[1] else null,
            desc = desc.formattedTextFrom(),
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
