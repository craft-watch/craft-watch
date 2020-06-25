package choliver.neapi

import choliver.neapi.Scraper.Result
import java.io.File

val CACHE_DIR = File("cache")

fun executeScraper(scraper: Scraper): List<Result.Item> {
  val getter = JsonGetter(HttpGetter(CACHE_DIR))
  return scraper.scrapeIndex(getter.request(scraper.rootUrl))
    .map { it.scrapeItem(getter.request(it.url)) }
    .filterIsInstance<Result.Item>()
}
