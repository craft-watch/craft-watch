package choliver.neapi.scrapers

import choliver.neapi.ParsedItem
import choliver.neapi.Scraper
import choliver.neapi.Scraper.Context
import java.net.URI

class HowlingHopsScraper : Scraper {
  override val name = "Howling Hops"

  override fun Context.scrape() = request(ROOT_URL) { doc -> doc
    .selectFirst(".wc-block-handpicked-products") // Avoid apparel
    .select(".wc-block-grid__product")
    .map { el ->
      val a = el.selectFirst(".wc-block-grid__product-link")
      val url = URI(a.attr("href").trim())

      val abv = request(url) { subDoc ->
        val result = "/ (\\d+(\\.\\d+)?)% ABV".toRegex().find(subDoc.text())
        result?.let { it.groupValues[1].trim().toBigDecimal() }
      }

      val rawName = a.selectFirst(".wc-block-grid__product-title").text().trim()
      val result = "^(.*?) (\\d+) x (\\d+)ml$".toRegex().find(rawName)!!
      val numCans = result.groupValues[2].toInt()

      ParsedItem(
        thumbnailUrl = URI(a.selectFirst(".attachment-woocommerce_thumbnail").attr("src").trim()),
        url = url,
        name = result.groupValues[1],
        available = true, // TODO
        abv = abv,
        sizeMl = result.groupValues[3].toInt(),
        price = el.select(".woocommerce-Price-amount")
          .filterNot { it.parent().tagName() == "del" } // Avoid non-sale price
          .first()
          .ownText()
          .toBigDecimal() / numCans.toBigDecimal()
      )
    }
    .groupBy { it.name }
    .values
    .map { group -> group.minBy { it.price }!! }  // Find best price for this beer
  }

  companion object {
    private val ROOT_URL = URI("https://www.howlinghops.co.uk/shop")
  }
}
