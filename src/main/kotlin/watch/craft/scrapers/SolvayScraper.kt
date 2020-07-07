package watch.craft.scrapers

import watch.craft.*
import watch.craft.Scraper.Job.Leaf
import watch.craft.Scraper.ScrapedItem
import java.net.URI

class SolvayScraper : Scraper {
  override val brewery = Brewery(
    shortName = "Solvay Society",
    name = "Solvay Society",
    location = "Leytonstone, London",
    websiteUrl = URI("https://www.solvaysociety.com/")
  )

  override val jobs = forRootUrls(*ROOT_URLS) { root ->
    root
      .selectMultipleFrom(".content .grid-item")
      .map { el ->
        val rawName = el.textFrom(".grid-title")

        Leaf(rawName, el.hrefFrom("a.grid-item-link")) { doc ->
          val nameParts = rawName.extract("(.*?)\\s+\\|\\s+(.*?)\\s+${ABV_REGEX}")
          // TODO - mixed pack

          val desc = doc.selectFrom(".ProductItem-details-excerpt")


          ScrapedItem(
            name = nameParts[1],
            summary = nameParts[2],
            desc = desc.formattedTextFrom(),
            abv = nameParts[3].toDouble(),
            sizeMl = desc.sizeMlFrom(),
            available = true,
            numItems = 1,
            price = el.priceFrom(".product-price"),
            thumbnailUrl = el.dataSrcFrom("img.grid-image-cover")
          )
        }
      }
  }

  companion object {
    private val ROOT_URLS = arrayOf(
      URI("https://www.solvaysociety.com/shop?category=Core+Range"),
      URI("https://www.solvaysociety.com/shop?category=Specials")
    )
  }
}
