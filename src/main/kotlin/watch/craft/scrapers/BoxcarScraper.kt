package watch.craft.scrapers

import watch.craft.Brewery
import watch.craft.Offer
import watch.craft.Scraper
import watch.craft.Scraper.Job.Leaf
import watch.craft.Scraper.ScrapedItem
import watch.craft.utils.*
import java.net.URI
import kotlin.text.RegexOption.IGNORE_CASE

class BoxcarScraper : Scraper {
  override val brewery = Brewery(
    shortName = "Boxcar",
    name = "Boxcar Brewery",
    location = "Bethnal Green, London",
    websiteUrl = URI("https://boxcarbrewery.co.uk/")
  )

  override val jobs = forRootUrls(ROOT_URL) { root ->
    root
      .shopifyItems()
      .map { details ->
        Leaf(details.title, details.url) { doc ->
          val parts = details.title.extract("^(.*?) // (.*?)% *(.*?)? //")

          ScrapedItem(
            thumbnailUrl = details.thumbnailUrl,
            name = parts[1],
            abv = parts[2].toDouble(),
            summary = parts[3].ifBlank { null },
            desc = doc.maybe { formattedTextFrom(".product-single__description") }
              ?.replace("^DESCRIPTION".toRegex(IGNORE_CASE), ""),
            available = details.available,
            offers = setOf(
              Offer(
                totalPrice = details.price,
                sizeMl = details.title.sizeMlFrom()
              )
            )
          )
        }
      }
  }

  companion object {
    private val ROOT_URL = URI("https://shop.boxcarbrewery.co.uk/collections/beer")
  }
}
