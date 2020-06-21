package choliver.neapi.scrapers

import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.math.BigDecimal
import java.net.URI

fun String.extract(regex: String) = regex.toRegex().find(this)?.groupValues

fun String.toTitleCase(): String = toLowerCase().split(" ").joinToString(" ") { it.capitalize() }

fun Element.textOf(cssQuery: String) = selectFirst(cssQuery).text().trim()

fun Element.hrefOf(cssQuery: String) = selectFirst(cssQuery).attr("href").trim()

fun Element.srcOf(cssQuery: String) = selectFirst(cssQuery).attr("src").trim()

data class ShopifyItemDetails(
  val title: String,
  val url: URI,
  val thumbnailUrl: URI,
  val price: BigDecimal,
  val available: Boolean
)

fun Document.shopifyItems(root: URI) = select(".product-card").map {
  ShopifyItemDetails(
    title = it.textOf(".product-card__title"),
    url = root.resolve(it.hrefOf(".grid-view-item__link")),
    thumbnailUrl = root.resolve(
      it.srcOf("noscript .grid-view-item__image")
        .replace("@2x", "")
        .replace("\\?.*".toRegex(), "")
    ),
    price = it.textOf(".price-item--sale").extract("\\d+\\.\\d+")!![0].toBigDecimal(),
    available = "price--sold-out" !in it.selectFirst(".price").classNames()
  )
}

