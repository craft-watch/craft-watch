package watch.craft

import java.net.URI

data class ScraperEntry(
  val scraper: Scraper,
  val brewery: Brewery
)

interface Scraper {
  val roots: List<Node>

  sealed class Node {
    data class Retrieval(
      val name: String? = null,
      val url: URI,
      val suffix: String,
      val validate: (data: ByteArray) -> Unit,  // TODO - reframe as "retryIf"
      val block: (data: ByteArray) -> List<Node>
    ) : Node()

    data class ScrapedItem(
      val name: String,
      val summary: String? = null,
      val desc: String? = null,
      val mixed: Boolean = false,
      val offers: Set<Offer> = emptySet(),
      val abv: Double? = null,
      val available: Boolean,
      val thumbnailUrl: URI,
      val url: URI? = null   // Defaults to retrieval URL
    ) : Node()
  }
}
