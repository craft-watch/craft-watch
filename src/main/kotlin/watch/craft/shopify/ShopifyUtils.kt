package watch.craft.shopify

import org.jsoup.nodes.Document
import watch.craft.MalformedInputException
import watch.craft.Offer
import watch.craft.utils.*
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
      .toUri(),
    price = it.priceFrom(".price-item--sale"),
    available = "price--sold-out" !in it.selectFrom(".price").classNames()
  )
}

data class ShopifyProduct(
  val variants: List<Variant>
) {
  data class Variant(
    val title: String,
    val price: Int
  )
}

fun Document.extractShopifyOffers(sizeMl: Int?) = extractShopifyProductInfo().variants
  .mapNotNull { variant ->
    val quantity = variant.title.maybe { extract("^(\\d+)").intFrom(1) }
    if (quantity == null) {
      null
    } else {
      Offer(
        quantity = quantity,
        totalPrice = variant.price / 100.0,
        sizeMl = sizeMl
      )
    }
  }
  .also { if (it.isEmpty()) throw MalformedInputException("Couldn't extract any offers") }
  .toSet()

fun Document.extractShopifyProductInfo() =
  jsonFrom<ShopifyProduct>("#ProductJson-product-template")
