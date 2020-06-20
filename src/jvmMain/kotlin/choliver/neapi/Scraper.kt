package choliver.neapi

import org.jsoup.nodes.Document
import java.net.URI

interface Scraper {
  val name: String
  fun Context.scrape(): List<ParsedItem>

  interface Context {
    fun <R> request(url: URI, block: (Document) -> R): R
  }
}
