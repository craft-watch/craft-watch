package watch.craft

import mu.KotlinLogging
import watch.craft.getters.CachingGetter
import watch.craft.getters.HtmlGetter
import watch.craft.getters.HttpGetter
import watch.craft.storage.*
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

private val logger = KotlinLogging.logger {}

private const val GOLDEN_DATE = "2020-06-28"

fun executeScraper(scraper: Scraper, dateString: String? = GOLDEN_DATE): List<Scraper.Item> {
  val live = dateString == null

  val instant = if (live) {
    Instant.now()
  } else {
    LocalDate.parse(dateString).atStartOfDay(ZoneOffset.UTC).toInstant()
  }

  val store = WriteThroughObjectStore(
    firstLevel = LocalObjectStore(CACHE_DIR),
    secondLevel = GcsObjectStore(GCS_BUCKET)
  ).let { if (live) it else ReadOnlyObjectStore(it) }

  val structure = StoreStructure(store, instant)
  val cachingGetter = CachingGetter(structure.cache, HttpGetter())
  val getter = HtmlGetter(cachingGetter)

  return scraper.rootUrls
    .flatMap { scraper.scrapeIndex(getter.request(it)) }
    .mapNotNull {
      try {
        it.scrapeItem(getter.request(it.url))
      } catch (e: SkipItemException) {
        logger.info("Skipped because: ${e.message}")
        null
      } catch (e: NonFatalScraperException) {
        logger.warn("Non-fatal exception: ${e.message}")
        null
      }
    }
}

fun List<Scraper.Item>.byName(name: String) = first { it.name == name }

fun Scraper.Item.noDesc() = copy(desc = null)    // Makes it easier to test item equality
