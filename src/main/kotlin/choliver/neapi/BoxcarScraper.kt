package choliver.neapi

import org.jsoup.nodes.Document
import java.net.URI

class BoxcarScraper : Scraper {
  override val name = "Boxcar"
  override val rootUrl = URI("https://shop.boxcarbrewery.co.uk/collections/beer")

  override fun scrape(doc: Document) = doc
    .select(".product-card")
    .map { el ->
      ParsedItem(
        url = URI(el.selectFirst(".grid-view-item__link").attr("href").trim()),
        name = el.selectFirst(".product-card__title").text().trim(),
        available = "price--sold-out" !in el.selectFirst(".price").classNames(),
        price = el.selectFirst(".price-item--sale")
          .text()
          .trim()
          .removePrefix("£")
          .toBigDecimal()
      )
    }
}
