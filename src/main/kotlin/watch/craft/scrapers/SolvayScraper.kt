package watch.craft.scrapers

import watch.craft.*
import watch.craft.Scraper.Job.Leaf
import watch.craft.Scraper.ScrapedItem
import watch.craft.utils.*
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
          val nameParts = rawName.extract("(.*?)\\s+\\|\\s+(?:(.*?)\\s+\\d)?")
          val desc = doc.selectFrom(".ProductItem-details-excerpt")
          val mixed = rawName.contains("mixed", ignoreCase = true)

          ScrapedItem(
            name = nameParts[1],
            summary = if (mixed) null else nameParts[2],
            desc = desc.formattedTextFrom(),
            mixed = mixed,
            keg = rawName.contains("keg", ignoreCase = true),
            abv = if (mixed) null else rawName.abvFrom(),
            sizeMl = if (mixed) null else desc.sizeMlFrom(),
            available = true,
            numItems = rawName.maybe { extract("(\\d+) pack") }?.get(1)?.toInt() ?: 1,
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
