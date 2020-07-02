package watch.craft

import watch.craft.Scraper.ScrapedItem
import watch.craft.executor.ScraperExecutor
import watch.craft.storage.*
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

private const val GOLDEN_DATE = "2020-06-30"

fun executeScraper(scraper: Scraper, dateString: String? = GOLDEN_DATE): List<ScrapedItem> {
  val live = dateString == null

  val instant = if (live) {
    Instant.now()
  } else {
    LocalDate.parse(dateString).atStartOfDay(ZoneOffset.UTC).toInstant()
  }

  val store = WriteThroughObjectStore(
    firstLevel = LocalObjectStore(CACHE_DIR),
    secondLevel = GcsObjectStore(GCS_BUCKET)
  )

  val structure = StoreStructure(store, instant)
  val getter = if (live) {
    CachingGetter(structure.cache)
  } else {
    CachingGetter(structure.cache) { throw FatalScraperException("Non-live tests should not perform network gets") }
  }
  return ScraperExecutor(getter, scraper).execute().map { it.item }
}

fun List<ScrapedItem>.byName(name: String) = first { it.name == name }

fun ScrapedItem.noDesc() = copy(desc = null)    // Makes it easier to test item equality
