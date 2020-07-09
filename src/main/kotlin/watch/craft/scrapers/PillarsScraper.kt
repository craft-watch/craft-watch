package watch.craft.scrapers

import watch.craft.Brewery
import watch.craft.Offer
import watch.craft.Scraper
import watch.craft.Scraper.Job.Leaf
import watch.craft.Scraper.ScrapedItem
import watch.craft.utils.*
import java.net.URI

class PillarsScraper : Scraper {
  override val brewery = Brewery(
    shortName = "Pillars",
    name = "Pillars Brewery",
    location = "Walthamstow, London",
    websiteUrl = URI("https://www.pillarsbrewery.com/")
  )

  override val jobs = forRootUrls(ROOT_URL) { root ->
    root
      .shopifyItems()
      .map { details ->
        Leaf(details.title, details.url) { doc ->
          val titleParts = extractTitleParts(details.title)
          val descParts = doc.orSkip("Couldn't find style or ABV") {
            extractFrom(".product-single__description", "STYLE:\\s+(.+?)\\s+ABV:\\s+(\\d\\.\\d+)%")
          }  // If we don't see these fields, assume we're not looking at a beer product

          ScrapedItem(
            thumbnailUrl = details.thumbnailUrl,
            name = titleParts.name,
            summary = descParts[1].toTitleCase(),
            desc = doc.maybe { formattedTextFrom(".product-single__description") },
            keg = titleParts.keg,
            sizeMl = titleParts.sizeMl,
            abv = descParts[2].toDouble(),
            available = details.available,
            offers = setOf(
              Offer(
                quantity = titleParts.numItems,
                totalPrice = details.price
              )
            )
          )
        }
      }
  }

  private data class TitleParts(
    val name: String,
    val sizeMl: Int? = null,
    val numItems: Int = 1,
    val keg: Boolean = false
  )

  private fun extractTitleParts(title: String) = when {
    title.contains("Case") -> {
      val parts = title.extract("(.*?) Case of (\\d+)")
      TitleParts(name = parts[1], numItems = parts[2].toInt())
    }
    title.contains("Keg") -> {
      TitleParts(
        name = title.extract("(.*?) \\d+")[1],
        sizeMl = title.sizeMlFrom(),
        keg = true
      )
    }
    else -> TitleParts(name = title)
  }

  companion object {
    private val ROOT_URL = URI("https://shop.pillarsbrewery.com/collections/pillars-beers")
  }
}
