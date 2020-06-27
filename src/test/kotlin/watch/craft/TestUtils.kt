package watch.craft

import mu.KotlinLogging
import watch.craft.getters.CachingGetter
import watch.craft.getters.HtmlGetter
import watch.craft.getters.HttpGetter
import watch.craft.storage.GcsObjectStore
import watch.craft.storage.LocalObjectStore
import watch.craft.storage.StoreStructure
import watch.craft.storage.WriteThroughObjectStore
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
        logger.warn("Non-fatal exception: ${e.message}")
        null
      }
    }
}


fun List<Scraper.Item>.byName(name: String) = first { it.name == name }

fun Scraper.Item.noDesc() = copy(desc = null)    // Makes it easier to test item equality
