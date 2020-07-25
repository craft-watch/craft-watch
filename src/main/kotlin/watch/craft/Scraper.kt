package watch.craft

import org.jsoup.nodes.Document
import watch.craft.Scraper.Output.Multiple
import java.net.URI

data class ScraperEntry(
  val scraper: Scraper,
  val brewery: Brewery
)

interface Scraper {
  val seed: Output

  sealed class Output {
    sealed class Work : Output() {
      abstract val url: URI
      abstract val name: String?

      data class JsonWork(
        override val url: URI,
        override val name: String? = null,
        val block: (data: Any) -> Output
      ) : Work()

      data class HtmlWork(
        override val url: URI,
        override val name: String? = null,
        val block: (data: Document) -> Output
      ) : Work()
    }

    data class Multiple(
      val outputs: List<Output>
    ) : Output()

    data class ScrapedItem(
      val name: String,
      val summary: String? = null,
      val desc: String? = null,
      val mixed: Boolean = false,
      val offers: Set<Offer> = emptySet(),
      val abv: Double? = null,
      val available: Boolean,
      val thumbnailUrl: URI
    ) : Output()
  }
}
