package choliver.neapi

import java.math.BigDecimal
import java.net.URI

data class ParsedItem(
  val name: String,
  val price: BigDecimal,
  val available: Boolean,
  val url: URI
)

data class Item(
  val brewery: String,
  val name: String,
  val price: BigDecimal,
  val available: Boolean,
  val url: URI
)
