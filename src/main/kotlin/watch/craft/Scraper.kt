package watch.craft

import org.jsoup.nodes.Document
import java.net.URI

data class ScraperEntry(
  val scraper: Scraper,
  val brewery: Brewery
)

interface Scraper {
  val root: Node

  sealed class Node {
    sealed class Work : Node() {
      abstract val name: String?
      abstract val url: URI

      data class JsonWork(
        override val name: String? = null,
        override val url: URI,
        val block: (data: Any) -> Node
      ) : Work()

      data class HtmlWork(
        override val name: String? = null,
        override val url: URI,
        val block: (data: Document) -> Node
      ) : Work()
    }

    data class Multiple(
      val nodes: List<Node> // TODO
    ) : Node()

    data class ScrapedItem(
      val name: String,
      val summary: String? = null,
      val desc: String? = null,
      val mixed: Boolean = false,
      val offers: Set<Offer> = emptySet(),
      val abv: Double? = null,
      val available: Boolean,
      val thumbnailUrl: URI
    ) : Node()
  }
}
