package choliver.neapi

import java.math.BigDecimal
import java.net.URI

data class ParsedItem(
  val name: String,
  val summary: String? = null,
  val pricePerCan: BigDecimal,
  val sizeMl: Int? = null,
  val abv: BigDecimal? = null,
  val available: Boolean,
  val thumbnailUrl: URI? = null,
  val url: URI
)
