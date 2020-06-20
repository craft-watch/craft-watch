package choliver.neapi

import org.jsoup.nodes.Document
import java.net.URI

interface Scraper {
  val name: String
  val rootUrl: URI
  fun scrape(doc: Document): List<ParsedItem>
}
