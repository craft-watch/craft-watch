package choliver.neapi.model

import kotlinx.serialization.Serializable

@Serializable
data class Inventory(
  val items: List<Item>
)

@Serializable
data class Item(
  val brewery: String,
  val name: String,
  val sizeMl: Int?,
  val abv: Float?,
  val price: Float,
  val available: Boolean,
  val thumbnailUrl: String?,
  val url: String
)
