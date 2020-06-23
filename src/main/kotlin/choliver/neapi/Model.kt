package choliver.neapi

import java.net.URI

data class Inventory(
  val items: List<Item>
)

data class Item(
  val brewery: String,
  val name: String,
  val summary: String?,
  val sizeMl: Int?,
  val abv: Double?,
  val perItemPrice: Double,
  val available: Boolean,
  val thumbnailUrl: String,
  val url: String
)

data class ParsedItem(
  val name: String,
  val summary: String? = null,
  val perItemPrice: Double,
  val sizeMl: Int? = null,
  val abv: Double? = null,
  val available: Boolean,
  val thumbnailUrl: URI,
  val url: URI
)
