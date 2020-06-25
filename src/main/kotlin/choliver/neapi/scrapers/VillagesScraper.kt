package choliver.neapi.scrapers

import choliver.neapi.*
import choliver.neapi.Scraper.Context
import java.net.URI

class VillagesScraper : Scraper {
  override val name = "Villages"

  override fun Context.scrape() = request(ROOT_URL)
    .shopifyItems()
    .map { details ->
      val itemText = request(details.url).text()
      val sizeMl = itemText.extract("(\\d+)ml")?.get(1)?.toInt()
      val parts = extractVariableParts(details.title)

      ScrapedItem(
        thumbnailUrl = details.thumbnailUrl,
        url = details.url,
        name = parts.name.toTitleCase(),
        summary = parts.summary,
        sizeMl = sizeMl,
        abv = parts.abv,
        available = details.available,
        perItemPrice = details.price.divideAsPrice(parts.numCans)
      )
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

  companion object {
    private val ROOT_URL = URI("https://villagesbrewery.com/collections/buy-beer")
  }
}
