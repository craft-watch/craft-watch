package choliver.neapi.scrapers

import choliver.neapi.ParsedItem
import choliver.neapi.Scraper
import choliver.neapi.Scraper.Context
import java.net.URI

class GipsyHillScraper : Scraper {
  override val name = "Gipsy Hill"

  override fun Context.scrape() = request(ROOT_URL)
    .select(".product")
    .map { el ->
      val a = el.selectFirst(".woocommerce-LoopProduct-link")
      val url = a.hrefFrom()
      val rawSummary = request(url).textFrom(".summary")

      val parts = rawSummary.extract("Sold as: ((\\d+) x )?(\\d+)ml")
      val numCans = parts?.get(2)?.toIntOrNull() ?: 1

      ParsedItem(
        thumbnailUrl = a.srcFrom(".attachment-woocommerce_thumbnail"),
        url = url,
        name = a.textFrom(".woocommerce-loop-product__title"),
        summary = rawSummary.extract("Style: (.*) ABV")?.get(1),
        available = true, // TODO
        abv = rawSummary.extract("ABV: (.*?)%")?.get(1)?.toDouble(),
        sizeMl = parts?.get(3)?.toInt(),
        perItemPrice = el.ownTextFrom(".woocommerce-Price-amount").toDouble().divideAsPrice(numCans)
      )
    }
    .distinctBy { it.name }

  companion object {
    private val ROOT_URL = URI("https://gipsyhillbrew.com")
  }
}
