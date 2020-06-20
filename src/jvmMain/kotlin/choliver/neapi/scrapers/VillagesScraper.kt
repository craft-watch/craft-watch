package choliver.neapi.scrapers

import choliver.neapi.ParsedItem
import choliver.neapi.Scraper
import choliver.neapi.Scraper.Context
import java.net.URI

class VillagesScraper : Scraper {
  override val name = "Villages"

  override fun Context.scrape() = request(ROOT_URL) { doc -> doc
    .select(".product-card")
    .map { el ->
      val rawName = el.selectFirst(".product-card__title").text()
      val result = "^(.*) (.*)%.*$".toRegex().find(rawName)

      val url = ROOT_URL.resolve(el.selectFirst(".grid-view-item__link").attr("href").trim())
      val subText = request(url) { it.text() }

      ParsedItem(
        thumbnailUrl = ROOT_URL.resolve(
          el.selectFirst("noscript .grid-view-item__image").attr("src").trim()
            .replace("@2x", "")
            .replace("\\?.*".toRegex(), "")
        ),
        url = url,
        name = (if (result != null) result.groupValues[1] else rawName).trim(),
        sizeMl = "(\\d+)ml".toRegex().find(subText)?.let {
          it.groupValues[1].trim().toInt()
        },
        abv = if (result != null) result.groupValues[2].trim().toBigDecimal() else null,
        available = "price--sold-out" !in el.selectFirst(".price").classNames(),
        pricePerCan = "\\d+\\.\\d+".toRegex().find(el.selectFirst(".price-item--sale").text())!!.value.toBigDecimal()
      )
    }
  }

  companion object {
    private val ROOT_URL = URI("https://villagesbrewery.com/collections/buy-beer")
  }
}
