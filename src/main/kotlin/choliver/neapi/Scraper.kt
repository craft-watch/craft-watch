package choliver.neapi

import org.jsoup.nodes.Document
import java.net.URI

interface Scraper<T> {
  val name: String
  fun Context.scrape(): List<ScrapedItem>

  fun Context.scrapeFoSho(): List<Pair<URI, (Document) -> ScrapedItem>> = emptyList()

  interface Context {
    fun request(url: URI): Document
  }
}
