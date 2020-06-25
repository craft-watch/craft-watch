package choliver.neapi

import java.io.File

val CACHE_DIR = File("cache")

fun executeScraper(scraper: Scraper): List<ScrapedItem> {
  val getter = JsonGetter(HttpGetter(CACHE_DIR))
  return scraper.scrapeIndex(getter.request(scraper.rootUrl))
    .mapNotNull { it.scrapeItem(getter.request(it.url)) }
}
