package choliver.neapi.scrapers

import choliver.neapi.ParsedItem
import choliver.neapi.Scraper
import org.jsoup.nodes.Document
import java.net.URI

class BoxcarScraper : Scraper {
  override val name = "Boxcar"
  override val rootUrl = URI("https://shop.boxcarbrewery.co.uk/collections/beer")

  override fun scrape(doc: Document) = doc
    .select(".product-card")
    .map { el ->
      val result = "^(.*) // (.*)%.*$".toRegex()
        .find(el.selectFirst(".product-card__title").text())!!

      ParsedItem(
        url = URI(el.selectFirst(".grid-view-item__link").attr("href").trim()),
        name = result.groupValues[1].trim(),
        abv = result.groupValues[2].trim().toBigDecimal(),
        available = "price--sold-out" !in el.selectFirst(".price").classNames(),
        price = el.selectFirst(".price-item--sale")
          .text()
          .trim()
          .removePrefix("£")
          .toBigDecimal()
      )
    }
}
