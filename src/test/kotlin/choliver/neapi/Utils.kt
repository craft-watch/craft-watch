package choliver.neapi

import choliver.neapi.Scraper.Result
import choliver.neapi.getters.CachingGetter
import choliver.neapi.getters.HtmlGetter
import choliver.neapi.getters.HttpGetter
import choliver.neapi.storage.GcsObjectStore
import choliver.neapi.storage.LocalObjectStore
import choliver.neapi.storage.StoreStructure
import choliver.neapi.storage.WriteThroughObjectStore
import java.time.Instant

fun executeScraper(scraper: Scraper): List<Result.Item> {
  val store = WriteThroughObjectStore(
    firstLevel = LocalObjectStore(CACHE_DIR),
    secondLevel = GcsObjectStore(GCS_BUCKET)
  )
  val cachingGetter = CachingGetter(
    StoreStructure(store, Instant.now()).htmlCache,
    HttpGetter()
  )
  val getter = HtmlGetter(cachingGetter)
  return scraper.scrapeIndex(getter.request(scraper.rootUrl))
    .map { it.scrapeItem(getter.request(it.url)) }
    .filterIsInstance<Result.Item>()
}
