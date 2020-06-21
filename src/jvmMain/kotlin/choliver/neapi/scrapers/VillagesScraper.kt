package choliver.neapi.scrapers

import choliver.neapi.ParsedItem
import choliver.neapi.Scraper
import choliver.neapi.Scraper.Context
import java.math.BigDecimal
import java.net.URI

class VillagesScraper : Scraper {
  override val name = "Villages"

  override fun Context.scrape() = request(ROOT_URL)
    .shopifyItems(ROOT_URL)
    .map { details ->
      val itemText = request(details.url).text()

      val numCans: Int
      val name: String
      val summary: String?
      val abv: BigDecimal?
      if (details.title.contains("mixed case", ignoreCase = true)) {
        val parts = details.title.extract("^(.*?) \\((.*)\\)$")!!
        name = parts[1].toTitleCase()
        abv = null
        numCans = 24
        summary = "${numCans} cans"
      } else {
        val parts = details.title.extract("^([^ ]*) (.*)? ((.*)%)?.*$")!!
        name = parts[1].toTitleCase()
        summary = parts[2]
        abv = parts[4].toBigDecimal()
        numCans = 12
      }

      ParsedItem(
        thumbnailUrl = details.thumbnailUrl,
        url = details.url,
        name = name,
        summary = summary,
        sizeMl = itemText.extract("(\\d+)ml")?.get(1)?.toInt(),
        abv = abv,
        available = details.available,
        pricePerCan = details.price / numCans.toBigDecimal()
      )
    }

  companion object {
    private val ROOT_URL = URI("https://villagesbrewery.com/collections/buy-beer")
  }
}
