package choliver.neapi.scrapers

import choliver.neapi.*
import choliver.neapi.Scraper.IndexEntry
import choliver.neapi.Scraper.Result.Item
import org.jsoup.nodes.Document
import java.net.URI

class VillagesScraper : Scraper {
  override val name = "Villages"
  override val rootUrl = URI("https://villagesbrewery.com/collections/buy-beer")

  override fun scrapeIndex(root: Document) = root
    .shopifyItems()
    .map { details ->
      IndexEntry(details.url) { doc ->
        val parts = extractVariableParts(details.title)

        Item(
          thumbnailUrl = details.thumbnailUrl,
          name = parts.name.toTitleCase(),
          summary = parts.summary,
          sizeMl = doc.extractFrom(regex = "(\\d+)ml")?.get(1)?.toInt(),
          abv = parts.abv,
          available = details.available,
          perItemPrice = details.price.divideAsPrice(parts.numCans)
        )
      }
    }

  private data class VariableParts(
    val name: String,
    val summary: String,
    val numCans: Int,
    val abv: Double? = null
  )

  private fun extractVariableParts(title: String) = if (title.contains("mixed case", ignoreCase = true)) {
    val parts = title.extract("^(.*?) \\((.*)\\)$")!!
    VariableParts(
      name = parts[1],
      summary = "24 cans",
      numCans = 24
    )
  } else {
    val parts = title.extract("^([^ ]*) (.*)? ((.*)%)?.*$")!!
    VariableParts(
      name = parts[1],
      summary = parts[2],
      numCans = 12,
      abv = parts[4].toDouble()
    )
  }
}
