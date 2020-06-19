package choliver.neapi

import org.jsoup.nodes.Document
import java.net.URI

class GipsyHillParser : Parser {
  override val rootUrl = URI("https://gipsyhillbrew.com")

  override fun parse(doc: Document) = doc
    .select(".product")
    .map { el ->
      val a = el.selectFirst(".woocommerce-LoopProduct-link")
      Item(
        url = URI(a.attr("href").trim()),
        name = a.selectFirst(".woocommerce-loop-product__title").text().trim(),
        available = true, // TODO
        price = el.selectFirst(".woocommerce-Price-amount").ownText().toBigDecimal()
      )
    }
    .distinctBy { it.name }
}
