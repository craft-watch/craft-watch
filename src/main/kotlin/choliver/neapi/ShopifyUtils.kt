package choliver.neapi

import org.jsoup.nodes.Document
import java.net.URI

data class ShopifyItemDetails(
  val title: String,
  val url: URI,
  val thumbnailUrl: URI,
  val price: Double,
  val available: Boolean
)

fun Document.shopifyItems() = selectMultipleFrom(".product-card").map {
  ShopifyItemDetails(
    title = it.textFrom(".product-card__title"),
    url = it.hrefFrom(".grid-view-item__link"),
    thumbnailUrl = it.srcFrom("noscript .grid-view-item__image")
      .toString()
      .replace("@2x", "")
      .replace("\\?.*".toRegex(), "")
      .toUri(),
    price = it.priceFrom(".price-item--sale"),
    available = "price--sold-out" !in it.selectFrom(".price").classNames()
  )
}
