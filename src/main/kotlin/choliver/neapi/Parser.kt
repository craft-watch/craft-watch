package choliver.neapi

import org.jsoup.nodes.Document
import java.net.URI

interface Parser {
  val rootUrl: URI
  fun parse(doc: Document): List<Item>
}
