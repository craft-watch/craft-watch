package choliver.neapi.scrapers

import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.math.BigDecimal
import java.net.URI

data class ShopifyItemDetails(
  val title: String,
  val url: URI,
  val thumbnailUrl: URI,
  val price: BigDecimal,
  val available: Boolean
)

fun Document.shopifyItems() = select(".product-card").map {
  ShopifyItemDetails(
    title = it.textFrom(".product-card__title"),
    url = URI(baseUri()).resolve(it.hrefFrom(".grid-view-item__link")),
    thumbnailUrl = URI(baseUri()).resolve(
      it.srcFrom("noscript .grid-view-item__image")
        .replace("@2x", "")
        .replace("\\?.*".toRegex(), "")
    ),
    price = it.priceFrom(".price-item--sale"),
    available = "price--sold-out" !in it.selectFirst(".price").classNames()
  )
}

fun Element.priceFrom(cssQuery: String = ":root") = extractFrom(cssQuery, "\\d+\\.\\d+")!![0].toBigDecimal()

fun Element.extractFrom(cssQuery: String = ":root", regex: String) = textFrom(cssQuery).extract(regex)

fun Element.textFrom(cssQuery: String = ":root") = selectFirst(cssQuery).text().trim()

fun Element.ownTextFrom(cssQuery: String = ":root") = selectFirst(cssQuery).ownText().trim()

fun Element.hrefFrom(cssQuery: String = ":root") = selectFirst(cssQuery).attr("href").trim()

fun Element.srcFrom(cssQuery: String = ":root") = selectFirst(cssQuery).attr("src").trim()

fun String.extract(regex: String) = regex.toRegex().find(this)?.groupValues

fun String.toTitleCase(): String = toLowerCase().split(" ").joinToString(" ") { it.capitalize() }


