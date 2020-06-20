package choliver.neapi.scrapers

import choliver.neapi.ParsedItem
import choliver.neapi.Scraper
import org.jsoup.nodes.Document
import java.net.URI

class HowlingHopsScraper : Scraper {
  override val name = "Howling Hops"
  override val rootUrl = URI("https://www.howlinghops.co.uk/shop")

  override fun scrape(doc: Document) = doc
    .selectFirst(".wc-block-handpicked-products") // Avoid apparel
    .select(".wc-block-grid__product")
    .map { el ->
      val a = el.selectFirst(".wc-block-grid__product-link")
      ParsedItem(
        url = URI(a.attr("href").trim()),
        name = a.selectFirst(".wc-block-grid__product-title").text().trim(),
        available = true, // TODO
        price = el.select(".woocommerce-Price-amount")
          .filterNot { it.parent().tagName() == "del" } // Avoid non-sale price
          .first()
          .ownText()
          .toBigDecimal()
      )
    }
}
