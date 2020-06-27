package watch.craft

import org.jsoup.nodes.Document
import java.net.URI

interface Scraper {
  val name: String
  val rootUrl: URI

  fun scrapeIndex(root: Document): List<IndexEntry>

  data class IndexEntry(
    val rawName: String,
    val url: URI,
    val scrapeItem: (doc: Document) -> Item
  )

  data class Item(
    val name: String,
    val summary: String? = null,
    val desc: String? = null,
    val keg: Boolean = false,
    val mixed: Boolean = false,
    val perItemPrice: Double,
    val sizeMl: Int? = null,
    val abv: Double? = null,
    val available: Boolean,
    val thumbnailUrl: URI
  )
}
