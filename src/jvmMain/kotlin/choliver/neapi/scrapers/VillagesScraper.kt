package choliver.neapi.scrapers

import choliver.neapi.ParsedItem
import choliver.neapi.Scraper
import org.jsoup.nodes.Document
import java.net.URI

class VillagesScraper : Scraper {
  override val name = "Villages"
  override val rootUrl = URI("https://villagesbrewery.com/collections/buy-beer")

  override fun scrape(doc: Document) = doc
    .select(".product-card")
    .map { el ->
      val rawName = el.selectFirst(".product-card__title").text()
      val result = "^(.*) (.*)%.*$".toRegex().find(rawName)

      ParsedItem(
        thumbnailUrl = URI(
          el.selectFirst("noscript .grid-view-item__image").attr("src").trim()
            .replace("@2x", "")
            .replace("\\?.*".toRegex(), "")
        ),
        url = URI(el.selectFirst(".grid-view-item__link").attr("href").trim()),
        name = (if (result != null) result.groupValues[1] else rawName).trim(),
        abv = if (result != null) result.groupValues[2].trim().toBigDecimal() else null,
        available = "price--sold-out" !in el.selectFirst(".price").classNames(),
        price = "\\d+\\.\\d+".toRegex().find(el.selectFirst(".price-item--sale").text())!!.value.toBigDecimal()
      )
    }
}
