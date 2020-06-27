package choliver.neapi

data class Inventory(
  val items: List<Item>
)

data class Item(
  val brewery: String,
  val name: String,
  val summary: String?,
  val keg: Boolean,
  val sizeMl: Int?,
  val abv: Double?,
  val perItemPrice: Double,
  val available: Boolean,
  val thumbnailUrl: String,
  val url: String
)
