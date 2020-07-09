package watch.craft.scrapers

import watch.craft.Brewery
import watch.craft.Scraper
import watch.craft.Scraper.Job.Leaf
import watch.craft.Scraper.ScrapedItem
import watch.craft.SkipItemException
import watch.craft.utils.*
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
          val title = doc.maybe { selectFrom(".itemTitle .small") }
          val parts = title?.maybe {
            extractFrom(regex = "(.*?)\\s+\\|\\s+(\\d+(\\.\\d+)?)%\\s+\\|\\s+((\\d+)\\s+x\\s+)?")
          } ?: throw SkipItemException("Could not extract details")

          val sizeMl = title.sizeMlFrom()
          ScrapedItem(
            thumbnailUrl = el.srcFrom(".imageInnerWrap img"),
            name = a.extractFrom(regex = "([^(]+)")[1].trim().toTitleCase(),
            summary = parts[1],
            desc = doc.formattedTextFrom(".about"),
            keg = (sizeMl >= 1000),
            abv = parts[2].toDouble(),
            sizeMl = sizeMl,
            available = ".unavailableItemWrap" !in doc,
            quantity = parts[5].ifBlank { "1" }.toInt(),
            totalPrice = el.extractFrom(".priceStandard", "£(\\d+\\.\\d+)")[1].toDouble()
          )
        }
      }
  }

  companion object {
    private val ROOT_URL = URI("https://shop.fivepointsbrewing.co.uk/browse/c-Beers-11")
  }
}
