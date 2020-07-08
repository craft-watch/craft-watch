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

class Setup(dateString: String? = null) {
  private val live = dateString == null

  private val instant = if (live) {
    Instant.now()
  } else {
    LocalDate.parse(dateString).atStartOfDay(ZoneOffset.UTC).toInstant()
  }

  private val store = WriteThroughObjectStore(
    firstLevel = LocalObjectStore(CACHE_DIR),
    secondLevel = GcsObjectStore(GCS_BUCKET)
  )

  private val downloads = NullObjectStore() // SubObjectStore(store, "${DOWNLOADS_DIR}/${DATE_FORMAT.format(instant)}")
  val results = SubObjectStore(store, RESULTS_DIRNAME)

  val createRetriever: (String) -> Retriever = { name ->
    CachingRetriever(
      downloads,
      if (live) NetworkRetriever(name) else FailingRetriever()
    )
  }

  companion object {
    const val DOWNLOADS_DIR = "downloads"
    const val RESULTS_DIRNAME = "results"

    private val DATE_FORMAT = DateTimeFormatter.ofPattern("YYYY-MM-dd").withZone(ZoneOffset.UTC)
  }
}
