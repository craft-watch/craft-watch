package choliver.ipaapi

import org.jsoup.nodes.Document

interface Parser {
  fun parse(doc: Document): List<Item>
}
