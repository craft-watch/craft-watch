package watch.craft.scrapers

import watch.craft.Brewery
import watch.craft.Scraper
import watch.craft.Scraper.Job.Leaf
import watch.craft.Scraper.ScrapedItem
import watch.craft.utils.*
import java.net.URI

class VillagesScraper : Scraper {
  override val brewery = Brewery(
    shortName = "Villages",
    name = "Villages Brewery",
    location = "Deptford, London",
    websiteUrl = URI("https://villagesbrewery.com/")
  )

  override val jobs = forRootUrls(ROOT_URL) { root ->
    root
      .shopifyItems()
      .map { details ->
        Leaf(details.title, details.url) { doc ->
          val parts = extractVariableParts(details.title)

          ScrapedItem(
            name = parts.name.toTitleCase(),
            summary = parts.summary,
            desc = doc.maybe { formattedTextFrom(".product-single__description") },
            mixed = parts.mixed,
            sizeMl = doc.maybe { sizeMlFrom() },
            abv = parts.abv,
            available = details.available,
            numItems = parts.numCans,
            price = details.price,
            thumbnailUrl = details.thumbnailUrl
          )
        }
      }
  }

  private data class VariableParts(
    val name: String,
    val summary: String? = null,
    val numCans: Int,
    val mixed: Boolean = false,
    val abv: Double? = null
  )

  private fun extractVariableParts(title: String) = if (title.contains("mixed case", ignoreCase = true)) {
    val parts = title.extract("^(.*?) \\((.*)\\)$")
    VariableParts(
      name = parts[1],
      numCans = 24,
      mixed = true
    )
  } else {
    val parts = title.extract("^([^ ]*) (.*)? .*%")
    VariableParts(
      name = parts[1],
      summary = parts[2],
      numCans = 12,
      abv = title.abvFrom()
    )
  }

  companion object {
    private val ROOT_URL = URI("https://villagesbrewery.com/collections/buy-beer")
  }
}
