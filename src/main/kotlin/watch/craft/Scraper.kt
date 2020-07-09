package watch.craft

import org.jsoup.nodes.Document
import java.net.URI

interface Scraper {
  val brewery: Brewery
  val jobs: List<Job>
  val rateLimitPeriodMillis: Int get() = 50   // TODO - eliminate this field?

  sealed class Job {
    open val name: String? = null
    abstract val url: URI

    data class More(
      override val url: URI,
      val work: (doc: Document) -> List<Job>
    ) : Job()

    data class Leaf(
      override val name: String,
      override val url: URI,
      val work: (doc: Document) -> ScrapedItem
    ) : Job()
  }

  data class ScrapedItem(
    val name: String,
    val summary: String? = null,
    val desc: String? = null,
    val keg: Boolean = false,
    val mixed: Boolean = false,
    val offers: Set<Offer> = emptySet(),
    val sizeMl: Int? = null,
    val abv: Double? = null,
    val available: Boolean,
    val thumbnailUrl: URI
  )
}
