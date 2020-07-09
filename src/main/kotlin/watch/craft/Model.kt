package watch.craft

import java.net.URI
import java.time.Instant

data class Inventory(
  val metadata: Metadata,
  val categories: List<String>,
  val breweries: List<Brewery>,
  val items: List<Item>
)

data class Metadata(
  val capturedAt: Instant
)

data class Brewery(
  val shortName: String,
  val name: String,
  val location: String,
  val websiteUrl: URI,
  val new: Boolean = false
)

data class Item(
  val brewery: String,
  val name: String,
  val summary: String? = null,
  val desc: String? = null,
  val mixed: Boolean = false,
  val abv: Double? = null,
  val offers: Set<Offer> = emptySet(),
  val available: Boolean,   // TODO - should this be part of the Offer?
  val categories: Set<String> = emptySet(),
  val new: Boolean = false,
  val thumbnailUrl: URI,
  val url: URI
)

data class Offer(
  val quantity: Int = 1,
  val totalPrice: Double,
  val sizeMl: Int? = null,
  val keg: Boolean = false
)

data class MinimalInventory(
  val items: List<MinimalItem>
)

data class MinimalItem(
  val brewery: String,
  val name: String
)
