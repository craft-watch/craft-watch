package choliver.neapi.scrapers

import choliver.neapi.*
import choliver.neapi.Scraper.Context
import java.net.URI
import kotlin.text.RegexOption.IGNORE_CASE

class CanopyScraper : Scraper {
  override val name = "Canopy"

  override fun Context.scrape() = request(ROOT_URL)
    .select(".grid-uniform")
    .take(3)  // Avoid merch
    .flatMap { it.select(".grid__item") }
    .filterNot { it.textFrom(".product__title").contains("box|pack".toRegex(IGNORE_CASE)) }  // Don't know how to extract number of can
    .map { el ->
      val a = el.selectFrom(".product__title a")
      val parts = a.extractFrom(regex = "([^\\d]+) (\\d+(\\.\\d+)?)?")!!
      val url = a.hrefFrom()
      val itemDoc = request(url)

      ParsedItem(
        thumbnailUrl = el.srcFrom(".grid__image img"),
        url = url,
        name = parts[1],
        summary = null,
        available = !(el.text().contains("Sold out", ignoreCase = true)),
        sizeMl = itemDoc.extractFrom(regex = "(\\d+)ml")!![1].toInt(),
        abv = if (parts[2].isBlank()) null else parts[2].toDouble(),
        perItemPrice = el.extractFrom(regex = "£(\\d+\\.\\d+)")!![1].toDouble()
      )
    }

  companion object {
    private val ROOT_URL = URI("https://shop.canopybeer.com/")
  }
}
