package watch.craft

import java.net.URI

data class ScraperEntry(
  val scraper: Scraper,
  val brewery: Brewery
)

interface Scraper {
  val root: Node

  sealed class Node {
    data class Work(
      val name: String? = null,
      val url: URI,
      val suffix: String,
      val validate: (data: ByteArray) -> Unit,
      val block: (data: ByteArray) -> Node
    ) : Node()

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
