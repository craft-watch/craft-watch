package choliver.neapi

import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.net.URI
import java.net.URISyntaxException
import kotlin.math.round

data class ShopifyItemDetails(
  val title: String,
  val url: URI,
  val thumbnailUrl: URI,
  val price: Double,
  val available: Boolean
)

fun Document.shopifyItems() = select(".product-card").map {
  ShopifyItemDetails(
    title = it.textFrom(".product-card__title"),
    url = it.hrefFrom(".grid-view-item__link"),
    thumbnailUrl = it.srcFrom("noscript .grid-view-item__image")
      .toString()
      .replace("@2x", "")
      .replace("\\?.*".toRegex(), "")
      .toUri(),
    price = it.priceFrom(".price-item--sale"),
    available = "price--sold-out" !in it.selectFirst(".price").classNames()
  )
}

fun List<ScrapedItem>.bestPricedItems() = groupBy { it.name }
  .values
  .map { group -> group.minBy { it.perItemPrice }!! }

fun Element.priceFrom(cssQuery: String = ":root") = extractFrom(cssQuery, "\\d+\\.\\d+")!![0].toDouble()
fun Element.extractFrom(cssQuery: String = ":root", regex: String) = textFrom(cssQuery).extract(regex)
fun Element.textFrom(cssQuery: String = ":root") = selectFrom(cssQuery).text().trim()
fun Element.ownTextFrom(cssQuery: String = ":root") = selectFrom(cssQuery).ownText().trim()
fun Element.hrefFrom(cssQuery: String = ":root") = attrFrom(cssQuery, "abs:href").toUri()
fun Element.srcFrom(cssQuery: String = ":root") = attrFrom(cssQuery, "abs:src").toUri()
fun Element.attrFrom(cssQuery: String = ":root", attr: String) = selectFrom(cssQuery).attr(attr)
  .ifBlank { throw ScraperException("Attribute blank or not present: ${attr}") }!!
fun Element.selectFrom(cssQuery: String) = selectFirst(cssQuery)
  ?: throw ScraperException("Element not present: ${cssQuery}")

fun String.extract(regex: String) = regex.toRegex().find(this)?.groupValues

fun String.toTitleCase(): String = split(" ").joinToString(" ") {
  if (it in BEER_WORDS) it else it.toLowerCase().capitalize()
}

// I *know* this doesn't really work for floating-point.  But it's good enough for our purposes.
fun Double.divideAsPrice(denominator: Int) = round(100 * this / denominator) / 100

private fun String.toUri() = try {
  URI(this)
} catch (e: URISyntaxException) {
  throw ScraperException("URL syntax error: ${this}", e)
}

private val BEER_WORDS = listOf(
  "IPL",
  "IPA",
  "DDH",
  "NEIPA",
  "DIPA"
)
