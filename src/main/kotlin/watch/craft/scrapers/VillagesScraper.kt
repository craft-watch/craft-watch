package watch.craft.scrapers

import org.jsoup.nodes.Document
import watch.craft.*
import watch.craft.Scraper.IndexEntry
import watch.craft.Scraper.Item
import java.net.URI

class VillagesScraper : Scraper {
  override val name = "Villages"
  override val rootUrls = listOf(URI("https://villagesbrewery.com/collections/buy-beer"))

  override fun scrapeIndex(root: Document) = root
    .shopifyItems()
    .map { details ->
      IndexEntry(details.title, details.url) { doc ->
        val parts = extractVariableParts(details.title)

        Item(
          thumbnailUrl = details.thumbnailUrl,
          name = parts.name.toTitleCase(),
          summary = parts.summary,
          desc = doc.maybeWholeTextFrom(".product-single__description")?.split("~")?.get(0),
          mixed = parts.mixed,
          sizeMl = doc.maybeExtractFrom(regex = "(\\d+)ml")?.get(1)?.toInt(),
          abv = parts.abv,
          available = details.available,
          numItems = parts.numCans,
          price = details.price
        )
      }
    }

  private data class VariableParts(
    val name: String,
    val summary: String,
    val numCans: Int,
    val mixed: Boolean = false,
    val abv: Double? = null
  )

  private fun extractVariableParts(title: String) = if (title.contains("mixed case", ignoreCase = true)) {
    val parts = title.extract("^(.*?) \\((.*)\\)$")
    VariableParts(
      name = parts[1],
      summary = "24 cans",
      numCans = 24,
      mixed = true
    )
  } else {
    val parts = title.extract("^([^ ]*) (.*)? ((.*)%)?.*$")
    VariableParts(
      name = parts[1],
      summary = parts[2],
      numCans = 12,
      abv = parts[4].toDouble()
    )
  }
}
