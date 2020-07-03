package watch.craft

import java.time.Instant

data class Inventory(
  val metadata: Metadata,
  val categories: List<String>,
  val items: List<Item>
)

data class Metadata(
  val capturedAt: Instant
)

data class Item(
  val brewery: String,
  val name: String,
  val summary: String? = null,
  val desc: String? = null,
  val keg: Boolean = false,
  val mixed: Boolean = false,
  val sizeMl: Int? = null,
  val abv: Double? = null,
  val numItems: Int = 1,
  val perItemPrice: Double,
  val available: Boolean,
  val categories: Set<String> = emptySet(),
  val thumbnailUrl: String,
  val url: String
)

data class MinimalInventory(
  val items: List<MinimalItem>
)

data class MinimalItem(
  val brewery: String,
  val name: String
)
