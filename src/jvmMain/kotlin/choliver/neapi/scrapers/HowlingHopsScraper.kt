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

      val name = a.selectFirst(".wc-block-grid__product-title").text().trim()

      ParsedItem(
        thumbnailUrl = URI(a.selectFirst(".attachment-woocommerce_thumbnail").attr("src").trim()),
        url = url,
        name = name,
        available = true, // TODO
        abv = abv,
        sizeMl = "(\\d+)ml".toRegex().find(name)?.let {
          it.groupValues[1].trim().toInt()
        },
        price = el.select(".woocommerce-Price-amount")
          .filterNot { it.parent().tagName() == "del" } // Avoid non-sale price
          .first()
          .ownText()
          .toBigDecimal()
      )
    }
  }

  companion object {
    private val ROOT_URL = URI("https://www.howlinghops.co.uk/shop")
  }
}
