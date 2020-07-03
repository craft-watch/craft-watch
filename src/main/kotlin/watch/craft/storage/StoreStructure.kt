package watch.craft.storage

import java.time.Instant
import java.time.ZoneOffset.UTC
import java.time.format.DateTimeFormatter

class StoreStructure(
  store: ObjectStore,
  start: Instant
) {
  val downloads = SubObjectStore(store, "${DOWNLOADS_DIR}/${DATE_FORMAT.format(start)}")
  val results = SubObjectStore(store, RESULTS_DIRNAME)

  companion object {
    const val DOWNLOADS_DIR = "downloads"
    const val RESULTS_DIRNAME = "results"

    private val DATE_FORMAT = DateTimeFormatter.ofPattern("YYYY-MM-dd").withZone(UTC)
  }
}
