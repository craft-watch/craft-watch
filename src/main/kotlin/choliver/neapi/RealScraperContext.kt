package choliver.neapi

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.net.URI

class RealScraperContext(private val getter: HttpGetter) : Scraper.Context {
  override fun request(url: URI): Document = try {
    Jsoup.parse(getter.get(url), url.toString())
  } catch (e: Exception) {
    throw ScraperException("Could not read page: ${url}", e)
  }
}
