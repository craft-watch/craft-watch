package choliver.neapi

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.net.URI

class RealScraperContext(private val getter: HttpGetter) : Scraper.Context {
  override fun <R> request(url: URI, block: (Document) -> R) = block(Jsoup.parse(getter.get(url)))
}
