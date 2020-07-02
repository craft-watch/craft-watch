package watch.craft

import java.time.Instant

data class Inventory(
  val metadata: Metadata,
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
  val categories: List<String> = emptyList(),
  val thumbnailUrl: String,
  val url: String
)
