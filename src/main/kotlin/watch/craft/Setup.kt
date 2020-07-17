package watch.craft

import watch.craft.network.CachingRetriever
import watch.craft.network.FailingRetriever
import watch.craft.network.NetworkRetriever
import watch.craft.network.Retriever
import watch.craft.storage.*
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

class Setup(
  dateString: String? = null,
  private val forceDownload: Boolean = false,
  firstLevelStore: ObjectStore = LocalObjectStore(LOCAL_STORAGE_DIR),
  secondLevelStore: ObjectStore = GcsObjectStore(GCS_BUCKET)
) {
  private val live = dateString == null

  private val instant = if (live) {
    Instant.now()
  } else {
    LocalDate.parse(dateString).atStartOfDay(ZoneOffset.UTC).toInstant()
  }

  private val store: ObjectStore = WriteThroughObjectStore(
    firstLevel = firstLevelStore,
    secondLevel = secondLevelStore
  )

  val results: ObjectStore = SubObjectStore(store, RESULTS_DIRNAME)

  val downloadsDir: ObjectStore = SubObjectStore(store, "${DOWNLOADS_DIR}/${calculateSubdir()}")

  val createRetriever: (String) -> Retriever = { name ->
    CachingRetriever(
      downloadsDir,
      if (live) {
        NetworkRetriever(name)
      } else {
        FailingRetriever()
      }
    )
  }

  private fun calculateSubdir(): String {
    val today = DATE_FORMAT.format(instant)

    val subdir = SubObjectStore(store, DOWNLOADS_DIR)
      .list()
      .sorted()
      .lastOrNull { it.startsWith(today) }
      ?: return today

    val parts = subdir.split("--")
    val idx = if (parts.size < 2) 0 else parts.last().toInt()
    val idxAdjusted = idx + if (forceDownload) 1 else 0

    return if (idxAdjusted == 0) {
      today
    } else {
      "${today}--${idxAdjusted.toString().padStart(3, '0')}"
    }
  }

  companion object {
    const val DOWNLOADS_DIR = "downloads"
    const val RESULTS_DIRNAME = "results"

    private val DATE_FORMAT = DateTimeFormatter.ofPattern("YYYY-MM-dd").withZone(ZoneOffset.UTC)
  }
}
