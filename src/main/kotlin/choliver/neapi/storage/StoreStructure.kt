package choliver.neapi.storage

import choliver.neapi.sha1
import mu.KotlinLogging
import java.io.FileNotFoundException
import java.time.Instant
import java.time.ZoneOffset.UTC
import java.time.format.DateTimeFormatter

class StoreStructure(
  private val store: ObjectStore,
  start: Instant
) {
  private val logger = KotlinLogging.logger {}

  private val todayDir = DATE_FORMAT.format(start)
  private val cacheDir = "${todayDir}/${CACHE_DIRNAME}"
  private val resultsDir = "${todayDir}/${RESULTS_DIRNAME}/${TIME_FORMAT.format(start)}"

  val htmlCache = object : HtmlCache {
    override fun write(key: String, text: String) {
      val fqk = htmlKey(key)
      store.write(fqk, text.toByteArray())
      logger.info("${key} written to cache: ${fqk}")
    }

    override fun read(key: String) = try {
      val fqk = htmlKey(key)
      String(store.read(fqk))
        .also { logger.info("${key} read from cache: ${fqk}") }
    } catch (e: FileNotFoundException) {
      null
    }
  }

  fun writeResults(key: String, content: ByteArray) {
    val fqk = "${resultsDir}/${key}"
    store.write(fqk, content)  // TODO - make key safe
    logger.info("Results written to: ${fqk}")
  }

  private fun htmlKey(key: String) = "${cacheDir}/${key.sha1()}.html"

  companion object {
    const val CACHE_DIRNAME = "cache"
    const val RESULTS_DIRNAME = "results"

    private val DATE_FORMAT = DateTimeFormatter.ofPattern("YYYY-MM-dd").withZone(UTC)
    private val TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss").withZone(UTC)
  }
}
