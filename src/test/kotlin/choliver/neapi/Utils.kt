package choliver.neapi

import choliver.neapi.Scraper.Result
import choliver.neapi.getters.CachingGetter
import choliver.neapi.getters.HtmlGetter
import choliver.neapi.getters.HttpGetter
import choliver.neapi.storage.GcsBacker
import choliver.neapi.storage.LocalBacker
import choliver.neapi.storage.StorageThinger
import java.time.Instant

fun executeScraper(scraper: Scraper): List<Result.Item> {
  val now = Instant.now()
  val remoteCachingGetter = CachingGetter(
    StorageThinger(GcsBacker(GCS_BUCKET), now),
    HttpGetter()
  )
  val localCachingGetter = CachingGetter(
    StorageThinger(LocalBacker(CACHE_DIR), now),
    remoteCachingGetter
  )
  val getter = HtmlGetter(localCachingGetter)
  return scraper.scrapeIndex(getter.request(scraper.rootUrl))
    .map { it.scrapeItem(getter.request(it.url)) }
    .filterIsInstance<Result.Item>()
}
