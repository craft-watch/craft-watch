package watch.craft

data class Inventory(
  val items: List<Item>
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
  val perItemPrice: Double,
  val available: Boolean,
  val thumbnailUrl: String,
  val thumbnailKey: String? = null,
  val url: String
)
