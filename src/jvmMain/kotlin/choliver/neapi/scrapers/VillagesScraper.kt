package choliver.neapi.scrapers

import choliver.neapi.ParsedItem
import choliver.neapi.Scraper
import choliver.neapi.Scraper.Context
import java.math.BigDecimal
import java.net.URI

class VillagesScraper : Scraper {
  override val name = "Villages"

  override fun Context.scrape() = request(ROOT_URL)
    .select(".product-card")
    .map { el ->
      val rawName = el.selectFirst(".product-card__title").text()

      val numCans: Int
      val name: String
      val summary: String?
      val abv: BigDecimal?
      if (rawName.contains("mixed case", ignoreCase = true)) {
        val result = "^(.*?) \\((.*)\\)$".toRegex().find(rawName)!!
        name = result.groupValues[1].toTitleCase()
        abv = null
        numCans = 24
        summary = "${numCans} cans"
      } else {
        val result = "^([^ ]*) (.*)? ((.*)%)?.*$".toRegex().find(rawName)!!
        name = result.groupValues[1].toTitleCase()
        summary = result.groupValues[2]
        abv = result.groupValues[4].toBigDecimal()
        numCans = 12
      }

      val url = ROOT_URL.resolve(el.selectFirst(".grid-view-item__link").attr("href").trim())
      val itemText = request(url).text()

      ParsedItem(
        thumbnailUrl = ROOT_URL.resolve(
          el.selectFirst("noscript .grid-view-item__image").attr("src").trim()
            .replace("@2x", "")
            .replace("\\?.*".toRegex(), "")
        ),
        url = url,
        name = name,
        summary = summary,
        sizeMl = "(\\d+)ml".toRegex().find(itemText)?.let {
          it.groupValues[1].trim().toInt()
        },
        abv = abv,
        available = "price--sold-out" !in el.selectFirst(".price").classNames(),
        pricePerCan = "\\d+\\.\\d+".toRegex().find(el.selectFirst(".price-item--sale").text())!!.value.toBigDecimal() / numCans.toBigDecimal()
      )
    }

  private fun String.toTitleCase(): String = toLowerCase().split(" ").joinToString(" ") { it.capitalize() }

  companion object {
    private val ROOT_URL = URI("https://villagesbrewery.com/collections/buy-beer")
  }
}
