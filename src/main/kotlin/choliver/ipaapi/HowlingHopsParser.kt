package choliver.ipaapi

import org.jsoup.nodes.Document
import java.net.URI

class HowlingHopsParser : Parser {
  override fun parse(doc: Document) =
    doc.select(".wc-block-grid__product")
      .map { el ->
        val a = el.selectFirst(".wc-block-grid__product-link")
        Item(
          url = URI(a.attr("href").trim()),
          name = a.selectFirst(".wc-block-grid__product-title").text().trim(),
          price = el.select(".woocommerce-Price-amount")
            .filterNot { it.parent().tagName() == "del" }
            .first()
            .ownText()
            .toBigDecimal()
        )
      }
}
