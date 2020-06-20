package choliver.neapi.scrapers

import choliver.neapi.ParsedItem
import choliver.neapi.Scraper
import org.jsoup.nodes.Document
import java.net.URI

class GipsyHillScraper : Scraper {
  override val name = "Gipsy Hill"
  override val rootUrl = URI("https://gipsyhillbrew.com")

  override fun scrape(doc: Document) = doc
    .select(".product")
    .map { el ->
      val a = el.selectFirst(".woocommerce-LoopProduct-link")
      ParsedItem(
        thumbnailUrl = URI(a.selectFirst(".attachment-woocommerce_thumbnail").attr("src").trim()),
        url = URI(a.attr("href").trim()),
        name = a.selectFirst(".woocommerce-loop-product__title").text().trim(),
        available = true, // TODO
        price = el.selectFirst(".woocommerce-Price-amount").ownText().toBigDecimal()
      )
    }
    .distinctBy { it.name }
}
