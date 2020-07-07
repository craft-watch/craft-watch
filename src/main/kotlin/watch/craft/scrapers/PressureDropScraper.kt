package watch.craft.scrapers

import watch.craft.*
import watch.craft.Scraper.Job.Leaf
import watch.craft.Scraper.ScrapedItem
import java.net.URI

class PressureDropScraper : Scraper {
  override val brewery = Brewery(
    shortName = "Pressure Drop",
    name = "Pressure Drop Brewing",
    location = "Tottenham, London",
    websiteUrl = URI("https://pressuredropbrewing.co.uk/")
  )

  override val jobs = forRootUrls(ROOT_URL) { root ->
    root
      .selectMultipleFrom(".product-grid-item")
      .map { el ->
        val a = el.selectFrom(".grid__image")
        val rawName = el.textFrom(".f--title")

        Leaf(rawName, a.hrefFrom()) { doc ->
          val itemText = doc.text()
          val parts = doc.extractFrom(".product__title", "^(.*?)\\s*(-\\s*(.*?))?$")

          if (parts[1].contains("box", ignoreCase = true)) {
            throw SkipItemException("Don't know how to identify number of cans for boxes")
          }

          ScrapedItem(
            thumbnailUrl = a.srcFrom("noscript img"),
            name = parts[1],
            summary = parts[3].ifBlank { null },
            desc = doc.maybe { formattedTextFrom(".product-description") },
            abv = itemText.maybe { extract("(\\d+(\\.\\d+)?)\\s*%") }?.get(1)?.toDouble(),
            sizeMl = itemText.maybe { sizeMlFrom() },
            available = true,
            price = doc.priceFrom(".ProductPrice")
          )
        }
      }
  }

  companion object {
    private val ROOT_URL = URI("https://pressuredropbrewing.co.uk/collections/beers")
  }
}
