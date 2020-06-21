package choliver.neapi

import java.net.URI

data class ParsedItem(
  val name: String,
  val summary: String? = null,
  val pricePerCan: Double,
  val sizeMl: Int? = null,
  val abv: Double? = null,
  val available: Boolean,
  val thumbnailUrl: URI? = null,
  val url: URI
)
