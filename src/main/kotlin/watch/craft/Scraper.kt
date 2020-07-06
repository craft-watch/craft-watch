package watch.craft

import org.jsoup.nodes.Document
import java.net.URI

interface Scraper {
  val brewery: Brewery
  val jobs: List<Job>

  sealed class Job {
    data class More(
      val url: URI,
      val work: (doc: Document) -> List<Job>
    ) : Job()

    data class Leaf(
      val rawName: String,
      val url: URI,
      val work: (doc: Document) -> ScrapedItem
    ) : Job()
  }

  data class ScrapedItem(
    val name: String,
    val summary: String? = null,
    val desc: String? = null,
    val keg: Boolean = false,
    val mixed: Boolean = false,
    val numItems: Int = 1,
    val price: Double,
    val sizeMl: Int? = null,
    val abv: Double? = null,
    val available: Boolean,
    val thumbnailUrl: URI
  )
}
