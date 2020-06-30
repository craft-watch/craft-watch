package watch.craft

import watch.craft.storage.*
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

private const val GOLDEN_DATE = "2020-06-28"

fun executeScraper(scraper: Scraper, dateString: String? = GOLDEN_DATE): List<Item> {
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
  val getter = CachingGetter(structure.cache)
  return Executor(getter).scrape(scraper).items
}

fun List<Item>.byName(name: String) = first { it.name == name }

fun Item.noDesc() = copy(desc = null)    // Makes it easier to test item equality
