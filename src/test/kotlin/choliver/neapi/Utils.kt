package choliver.neapi

import choliver.neapi.Scraper.Result
import choliver.neapi.getters.CachingGetter
import choliver.neapi.getters.HtmlGetter
import choliver.neapi.getters.HttpGetter
import choliver.neapi.storage.GcsBacker
import choliver.neapi.storage.StorageThinger
import java.time.Instant

fun executeScraper(scraper: Scraper): List<Result.Item> {
  val backer = GcsBacker(GCS_BUCKET)
  val storage = StorageThinger(backer, Instant.now())
  val getter = HtmlGetter(CachingGetter(storage, HttpGetter()))
  return scraper.scrapeIndex(getter.request(scraper.rootUrl))
    .map { it.scrapeItem(getter.request(it.url)) }
    .filterIsInstance<Result.Item>()
}
