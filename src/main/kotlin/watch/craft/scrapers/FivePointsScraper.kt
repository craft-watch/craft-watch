package watch.craft.scrapers

import watch.craft.*
import watch.craft.Scraper.Job.Leaf
import watch.craft.Scraper.ScrapedItem
import java.net.URI

class FivePointsScraper : Scraper {
  override val brewery = Brewery(
    shortName = "Five Points",
    name = "The Five Points Brewing Co",
    location = "Hackney, London",
    websiteUrl = URI("https://fivepointsbrewing.co.uk/")
  )

  override val jobs = forRootUrls(ROOT_URL) { root ->
    root
      .selectMultipleFrom("#browse li .itemWrap")
      .map { el ->
        val a = el.selectFrom("h2 a")

        Leaf(a.text(), a.hrefFrom()) { doc ->
          val parts = doc.maybeExtractFrom(
            ".itemTitle .small",
            "(.*?)\\s+\\|\\s+(\\d+(\\.\\d+)?)%\\s+\\|\\s+((\\d+)\\s+x\\s+)?(\\d+)(ml|L)"
          ) ?: throw SkipItemException("Could not extract details")

          val sizeMl = parts[6].toInt() * (if (parts[7] == "L") 1000 else 1)
          ScrapedItem(
            thumbnailUrl = el.srcFrom(".imageInnerWrap img"),
            name = a.extractFrom(regex = "([^(]+)")[1].trim().toTitleCase(),
            summary = parts[1],
            desc = doc.normaliseParagraphsFrom(".about"),
            keg = (sizeMl >= 1000),
            abv = parts[2].toDouble(),
            sizeMl = sizeMl,
            available = doc.maybeSelectFrom(".unavailableItemWrap") == null,
            numItems = parts[5].ifBlank { "1" }.toInt(),
            price = el.extractFrom(".priceStandard", "£(\\d+\\.\\d+)")[1].toDouble()
          )
        }
      }
  }

  companion object {
    private val ROOT_URL = URI("https://shop.fivepointsbrewing.co.uk/browse/c-Beers-11")
  }
}
