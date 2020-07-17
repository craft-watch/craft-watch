package watch.craft

import java.net.URI
import java.time.Instant

// Denotes a field that has Set semantics, but we're using a List to enforce deterministic serialisation ordering
annotation class SemanticallyASet

data class Inventory(
  val metadata: Metadata,
  val stats: Stats = Stats(),
  val categories: List<String>,
  val breweries: List<Brewery>,
  val items: List<Item>
)

data class Metadata(
  val capturedAt: Instant,
  val ciBranch: String? = null
)

data class Stats(
  @SemanticallyASet
  val breweries: List<BreweryStats> = emptyList()
)

data class BreweryStats(
  val breweryId: String,
  val numRawItems: Int = 0,
  val numSkipped: Int = 0,
  val numMalformed: Int = 0,
  val numInvalid: Int = 0,
  val numErrors: Int = 0,
  val numMerged: Int = 0
  // TODO - numNew
)

data class Brewery(
  val id: String,
  val shortName: String,
  val name: String,
  val location: String,
  val websiteUrl: URI,
  val twitterHandle: String? = null,
  val new: Boolean = false
)

data class Item(
  val breweryId: String,
  val name: String,
  val summary: String? = null,
  val desc: String? = null,
  val mixed: Boolean = false,
  val abv: Double? = null,
  @SemanticallyASet
  val offers: List<Offer> = emptyList(),
  val available: Boolean,   // TODO - should this be part of the Offer?
  @SemanticallyASet
  val categories: List<String> = emptyList(),
  val new: Boolean = false,
  val thumbnailUrl: URI,
  val url: URI
)

data class Offer(
  val quantity: Int = 1,
  val totalPrice: Double,
  val sizeMl: Int? = null,
  val format: Format? = null
)

enum class Format {
  BOTTLE,
  CAN,
  KEG
}

data class MinimalInventory(
  val items: List<MinimalItem>
)

data class MinimalItem(
  val breweryId: String,
  val name: String
)
