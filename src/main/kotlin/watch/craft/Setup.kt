package watch.craft

import watch.craft.storage.*
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

class Setup(dateString: String? = null) {
  private val live = dateString == null

  private val instant = if (live) {
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
}
