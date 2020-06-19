package choliver.neapi

import org.jsoup.nodes.Document
import java.net.URI

class VillagesParser : Parser {
  override val rootUrl = URI("https://villagesbrewery.com/collections/buy-beer")

  override fun parse(doc: Document) = doc
    .select(".product-card")
    .map { el ->
      Item(
        url = URI(el.selectFirst(".grid-view-item__link").attr("href").trim()),
        name = el.selectFirst(".product-card__title").text().trim(),
        available = "price--sold-out" !in el.selectFirst(".price").classNames(),
        price = "\\d+\\.\\d+".toRegex().find(el.selectFirst(".price-item--sale").text())!!.value.toBigDecimal()
      )
    }
}
