package choliver.neapi

import java.io.File

val CACHE_DIR = File("cache")

fun executeScraper(scraper: Scraper): List<ScrapedItem> {
  val getter = HttpGetter(CACHE_DIR)
  val ctx = RealScraperContext(getter)
  return with(scraper) { ctx.scrape() }
}
