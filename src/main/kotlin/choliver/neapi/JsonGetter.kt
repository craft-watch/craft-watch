package choliver.neapi

import org.jsoup.Jsoup
import java.net.URI

class JsonGetter(private val getter: HttpGetter) {
  fun request(url: URI) = try {
    Jsoup.parse(getter.get(url), url.toString())
  } catch (e: Exception) {
    throw ScraperException("Could not read page: ${url}", e)
  }
}
