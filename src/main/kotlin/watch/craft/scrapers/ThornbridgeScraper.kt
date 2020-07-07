package watch.craft.scrapers

import watch.craft.*
import watch.craft.Scraper.Job.Leaf
import watch.craft.Scraper.ScrapedItem
import watch.craft.utils.*
import java.net.URI
import kotlin.text.RegexOption.IGNORE_CASE

class ThornbridgeScraper : Scraper {
  override val brewery = Brewery(
    shortName = "Thornbridge",
    name = "Thornbridge Brewery",
    location = "Bakewell, Derbyshire",
    websiteUrl = URI("https://thornbridgebrewery.co.uk/")
  )

  override val jobs = forRootUrls(ROOT_URL) { root ->
    root
      .selectMultipleFrom(".grid-uniform > .grid-item")
      .map { el ->
        val rawName = el.textFrom(".h6")

        Leaf(rawName, el.hrefFrom("a")) { doc ->
          val abv = orSkip("No ABV in title, so assume it's not a beer") { rawName.abvFrom() }

          val parts = rawName.extract("(.*?)\\W+\\d.*%\\W+(.*)")
          val desc = doc.selectFrom(".product-description")

          // TODO - identify mixed packs

          ScrapedItem(
            thumbnailUrl = doc.srcFrom(".product__image-wrapper img"),
            name = parts[1].replace(" (bottle|can)$".toRegex(IGNORE_CASE), ""),
            summary = parts[2],
            desc = desc.formattedTextFrom(),
            mixed = false,
            sizeMl = desc.maybe { sizeMlFrom() },
            abv = abv,
            available = "sold-out" !in el.classNames(),
            numItems = desc.maybe { extractFrom(regex = "(\\d+)\\s*x") }?.get(1)?.toInt()
              ?: rawName.maybe { extract(regex = "(\\d+)\\s*x") }?.get(1)?.toInt()
              ?: 1,
            price = el.priceFrom(".product-item--price")
          )
        }
      }
  }

  companion object {
    // TODO - URI("https://shop.thornbridgebrewery.co.uk/collections/smart-collection?view=list")
    private val ROOT_URL = URI("https://shop.thornbridgebrewery.co.uk/collections/pick-and-mix-beers?view=list")
  }
}
