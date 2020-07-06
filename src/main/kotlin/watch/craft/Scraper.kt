package watch.craft

import org.jsoup.nodes.Document
import watch.craft.Scraper.Job.Leaf
import watch.craft.Scraper.Job.More
import java.net.URI

interface Scraper {
  val brewery: Brewery
  val rootUrls: List<URI>

  fun scrapeIndex(root: Document): List<IndexEntry>

  // TODO - eliminate this
  val rootJobs: List<Job> get() = rootUrls
    .map { url ->
      More(url = url) { doc ->
        scrapeIndex(doc).map { entry ->
          Leaf(rawName = entry.rawName, url = entry.url, work = entry.scrapeItem)
        }
      }
    }

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

  data class IndexEntry(
    val rawName: String,
    val url: URI,
    val scrapeItem: (doc: Document) -> ScrapedItem
  )

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
