package choliver.neapi.scrapers

import choliver.neapi.ParsedItem
import choliver.neapi.Scraper
import choliver.neapi.Scraper.Context
import java.net.URI

class PressureDropScraper : Scraper {
  override val name = "Pressure Drop"

  override fun Context.scrape() = request(ROOT_URL)
    .select(".product-grid-item")
    .map { el ->
      val a = el.selectFirst(".grid__image")
      val url = ROOT_URL.resolve(a.attr("href").trim())

      val itemDoc = request(url)
      val itemText = itemDoc.text()

      val rawName = itemDoc.selectFirst(".product__title").text().trim()
      val result = "^(.*?)\\s*-\\s*(.*?)$".toRegex().find(rawName)!!

      ParsedItem(
        thumbnailUrl = ROOT_URL.resolve(a.selectFirst("noscript img").attr("src").trim()),
        url = url,
        name = result.groupValues[1],
        summary = result.groupValues[2],
        abv = "(\\d+(\\.\\d+)?)\\s*%".toRegex().find(itemText)?.let {
          it.groupValues[1].trim().toBigDecimal()
        },
        sizeMl = "(\\d+)ml".toRegex().find(itemText)?.let {
          it.groupValues[1].trim().toInt()
        },
        available = true,
        pricePerCan = itemDoc.selectFirst(".ProductPrice").text().trim().removePrefix("£").toBigDecimal()
      )
    }

  companion object {
    private val ROOT_URL = URI("https://pressuredropbrewing.co.uk/collections/beers")
  }
}
