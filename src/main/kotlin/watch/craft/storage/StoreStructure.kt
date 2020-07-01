package watch.craft.storage

import java.time.Instant
import java.time.ZoneOffset.UTC
import java.time.format.DateTimeFormatter

class StoreStructure(
  store: ObjectStore,
  start: Instant
) {
  private val todayDir = DATE_FORMAT.format(start)

  val blobs = SubObjectStore(store, BLOBS_DIRNAME)
  val cache = SubObjectStore(store, "$todayDir/$CACHE_DIRNAME")
  val results = SubObjectStore(store, "$todayDir/$RESULTS_DIRNAME/${TIME_FORMAT.format(start)}")

  companion object {
    const val BLOBS_DIRNAME = "blobs"
    const val CACHE_DIRNAME = "cache"
    const val RESULTS_DIRNAME = "results"

    private val DATE_FORMAT = DateTimeFormatter.ofPattern("YYYY-MM-dd").withZone(UTC)
    private val TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss").withZone(UTC)
  }
}
