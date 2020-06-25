package choliver.neapi

import org.jsoup.nodes.Document
import java.net.URI

interface Scraper {
  val name: String
  val rootUrl: URI

  fun scrapeIndex(root: Document): List<IndexEntry>

  interface Context {
    fun request(url: URI): Document
  }

  data class IndexEntry(
    val url: URI,
    val scrapeItem: (doc: Document) -> ScrapedItem?
  )
}
