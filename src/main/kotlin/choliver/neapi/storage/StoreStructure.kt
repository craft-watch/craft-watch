package choliver.neapi.storage

import java.time.Instant
import java.time.ZoneOffset.UTC
import java.time.format.DateTimeFormatter

class StoreStructure(
  store: ObjectStore,
  start: Instant
) {
  private val todayDir = DATE_FORMAT.format(start)

  val cache = SubObjectStore(
    "$todayDir/$CACHE_DIRNAME",
    store
  )

  val results = SubObjectStore(
    "$todayDir/$RESULTS_DIRNAME/${TIME_FORMAT.format(start)}",
    store
  )

  companion object {
    const val CACHE_DIRNAME = "cache"
    const val RESULTS_DIRNAME = "results"

    private val DATE_FORMAT = DateTimeFormatter.ofPattern("YYYY-MM-dd").withZone(UTC)
    private val TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss").withZone(UTC)
  }
}
