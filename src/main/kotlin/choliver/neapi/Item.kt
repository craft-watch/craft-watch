package choliver.neapi

import java.math.BigDecimal
import java.net.URI

data class Item(
  val name: String,
  val price: BigDecimal,
  val url: URI
)
