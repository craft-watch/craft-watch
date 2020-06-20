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
  val abv: Float?, // TODO
  val price: Float, // TODO
  val available: Boolean,
  val url: String   // TODO
)
