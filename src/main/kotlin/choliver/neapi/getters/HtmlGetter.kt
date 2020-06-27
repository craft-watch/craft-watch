package choliver.neapi.getters

import choliver.neapi.FatalException
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.net.URI

class HtmlGetter(private val getter: Getter<String>) : Getter<Document> {
  override fun request(url: URI) = try {
    Jsoup.parse(getter.request(url), url.toString())!!
  } catch (e: Exception) {
    throw FatalException("Could not read page: ${url}", e)
  }
}
