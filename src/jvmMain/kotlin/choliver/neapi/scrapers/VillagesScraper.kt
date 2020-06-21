package choliver.neapi.scrapers

import choliver.neapi.ParsedItem
import choliver.neapi.Scraper
import choliver.neapi.Scraper.Context
import java.net.URI

class VillagesScraper : Scraper {
  override val name = "Villages"

  override fun Context.scrape() = request(ROOT_URL)
    .shopifyItems()
    .map { details ->
      val itemText = request(details.url).text()
      val sizeMl = itemText.extract("(\\d+)ml")?.get(1)?.toInt()

      if (details.title.contains("mixed case", ignoreCase = true)) {
        val numCans = 24
        val parts = details.title.extract("^(.*?) \\((.*)\\)$")!!
        ParsedItem(
          thumbnailUrl = details.thumbnailUrl,
          url = details.url,
          name = parts[1].toTitleCase(),
          summary = "${numCans} cans",
          sizeMl = sizeMl,
          available = details.available,
          pricePerCan = details.price / numCans.toBigDecimal()
        )
      } else {
        val parts = details.title.extract("^([^ ]*) (.*)? ((.*)%)?.*$")!!
        ParsedItem(
          thumbnailUrl = details.thumbnailUrl,
          url = details.url,
          name = parts[1].toTitleCase(),
          summary = parts[2],
          sizeMl = sizeMl,
          abv = parts[4].toBigDecimal(),
          available = details.available,
          pricePerCan = details.price / 12.toBigDecimal()
        )
      }
    }

  companion object {
    private val ROOT_URL = URI("https://villagesbrewery.com/collections/buy-beer")
  }
}
