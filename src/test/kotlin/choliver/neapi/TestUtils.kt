package choliver.neapi

import choliver.neapi.getters.CachingGetter
import choliver.neapi.getters.HtmlGetter
import choliver.neapi.getters.HttpGetter
import choliver.neapi.storage.GcsObjectStore
import choliver.neapi.storage.LocalObjectStore
import choliver.neapi.storage.StoreStructure
import choliver.neapi.storage.WriteThroughObjectStore
import mu.KotlinLogging
import java.time.Instant

private val logger = KotlinLogging.logger {}

fun executeScraper(scraper: Scraper): List<Scraper.Item> {
  val store = WriteThroughObjectStore(
    firstLevel = LocalObjectStore(CACHE_DIR),
    secondLevel = GcsObjectStore(GCS_BUCKET)
  )
  val structure = StoreStructure(store, Instant.now())
  val cachingGetter = CachingGetter(structure.cache, HttpGetter())
  val getter = HtmlGetter(cachingGetter)
  return scraper.scrapeIndex(getter.request(scraper.rootUrl))
    .mapNotNull {
      try {
        it.scrapeItem(getter.request(it.url))
      } catch (e: NonFatalScraperException) {
        logger.warn("Non-fatal exception", e)
        null
      }
    }
}
