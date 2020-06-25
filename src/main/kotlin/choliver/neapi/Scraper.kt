package choliver.neapi

import org.jsoup.nodes.Document
import java.net.URI

interface Scraper {
  val name: String
  val rootUrl: URI

  fun scrapeIndex(root: Document): List<IndexEntry>

  data class IndexEntry(
    val url: URI,
    val scrapeItem: (doc: Document) -> Result
  )

  sealed class Result {
    data class Skipped(val reason: String) : Result()
    data class Item(
      val name: String,
      val summary: String? = null,
      val perItemPrice: Double,
      val sizeMl: Int? = null,
      val abv: Double? = null,
      val available: Boolean,
      val thumbnailUrl: URI
    ) : Result()

  }
}
