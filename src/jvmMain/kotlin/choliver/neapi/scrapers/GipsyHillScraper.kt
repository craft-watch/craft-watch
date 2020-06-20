package choliver.neapi.scrapers

import choliver.neapi.ParsedItem
import choliver.neapi.Scraper
import choliver.neapi.Scraper.Context
import java.net.URI

class GipsyHillScraper : Scraper {
  override val name = "Gipsy Hill"

  override fun Context.scrape() = request(ROOT_URL) { doc -> doc
    .select(".product")
    .map { el ->
      val a = el.selectFirst(".woocommerce-LoopProduct-link")
      val url = URI(a.attr("href").trim())

      val abv = request(url) { subDoc ->
        val result = "ABV: (.*?)%".toRegex().find(subDoc.text())
        result?.let { it.groupValues[1].trim().toBigDecimal() }
      }

      ParsedItem(
        thumbnailUrl = URI(a.selectFirst(".attachment-woocommerce_thumbnail").attr("src").trim()),
        url = url,
        name = a.selectFirst(".woocommerce-loop-product__title").text().trim(),
        available = true, // TODO
        abv = abv,
        price = el.selectFirst(".woocommerce-Price-amount").ownText().toBigDecimal()
      )
    }
    .distinctBy { it.name }
  }

  companion object {
    private val ROOT_URL = URI("https://gipsyhillbrew.com")
  }
}
