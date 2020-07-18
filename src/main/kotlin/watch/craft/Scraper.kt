package watch.craft

import org.jsoup.nodes.Document
import java.net.URI

data class ScraperEntry(
  val scraper: Scraper,
  val brewery: Brewery
)

interface Scraper {
  val jobs: List<Job>

  sealed class Job {
    open val name: String? = null
    abstract val url: URI
    abstract val sanityCheck: (doc: Document) -> Boolean

    data class More(
      override val url: URI,
      override val sanityCheck: (doc: Document) -> Boolean = { true },
      val work: (doc: Document) -> List<Job>
    ) : Job()

    data class Leaf(
      override val name: String,
      override val url: URI,
      override val sanityCheck: (doc: Document) -> Boolean = { true },
      val work: (doc: Document) -> ScrapedItem
    ) : Job()
  }

  data class ScrapedItem(
    val name: String,
    val summary: String? = null,
    val desc: String? = null,
    val mixed: Boolean = false,
    val offers: Set<Offer> = emptySet(),
    val abv: Double? = null,
    val available: Boolean,
    val thumbnailUrl: URI
  )
}
