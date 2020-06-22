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
      val url = ROOT_URL.resolve(a.hrefFrom())

      val itemDoc = request(url)
      val itemText = itemDoc.text()

      val parts = itemDoc.extractFrom(".product__title", "^(.*?)\\s*-\\s*(.*?)$")!!

      ParsedItem(
        thumbnailUrl = ROOT_URL.resolve(a.srcFrom("noscript img")),
        url = url,
        name = parts[1],
        summary = parts[2],
        abv = itemText.extract("(\\d+(\\.\\d+)?)\\s*%")?.get(1)?.toDouble(),  // TODO - deal with all the ?
        sizeMl = itemText.extract("(\\d+)ml")?.get(1)?.toInt(),
        available = true,
        unitPrice = itemDoc.priceFrom(".ProductPrice")
      )
    }

  companion object {
    private val ROOT_URL = URI("https://pressuredropbrewing.co.uk/collections/beers")
  }
}
