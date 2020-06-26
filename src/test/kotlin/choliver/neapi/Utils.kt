package choliver.neapi

import choliver.neapi.Scraper.Result
import choliver.neapi.getters.HttpGetter
import choliver.neapi.getters.cached
import choliver.neapi.getters.toHtml

fun executeScraper(scraper: Scraper): List<Result.Item> {
  val getter = HttpGetter().cached(CACHE_DIR).toHtml()
  return scraper.scrapeIndex(getter.request(scraper.rootUrl))
    .map { it.scrapeItem(getter.request(it.url)) }
    .filterIsInstance<Result.Item>()
}
