package choliver.neapi.storage

import choliver.neapi.sha1
import mu.KotlinLogging
import java.io.File
import java.io.FileNotFoundException
import java.time.Instant
import java.time.ZoneOffset
import java.time.ZoneOffset.UTC
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatter.ISO_DATE
import java.time.format.DateTimeFormatter.ISO_TIME

class StorageThinger(
  private val rootDir: File,
  timestamp: Instant
) {
  private val logger = KotlinLogging.logger {}

  private val todayDir = rootDir.resolve(DATE_FORMAT.format(timestamp))
  private val cacheDir = todayDir.resolve(CACHE_DIRNAME)
  private val resultsDir = todayDir.resolve(RESULTS_DIRNAME).resolve(TIME_FORMAT.format(timestamp))

  fun readFromHtmlCache(key: String) = try {
    ISO_TIME
    val file = htmlFileInCache(key)
    file.readText()
      .also { logger.info("${key} read from cache: ${file.friendly()}") }
  } catch (e: FileNotFoundException) {
    null
  }

  fun writeToHtmlCache(key: String, text: String) {
    val file = htmlFileInCache(key)
    file.createAndWrite(text.toByteArray())
    logger.info("${key} written to cache: ${file.friendly()}")
  }

  fun writeResults(key: String, data: ByteArray) {
    resultsDir.resolve(key).createAndWrite(data)  // TODO - make key safe
  }

  private fun File.createAndWrite(data: ByteArray) {
    parentFile.mkdirs()
    writeBytes(data)
  }

  private fun htmlFileInCache(key: String) = cacheDir.resolve(key.sha1() + ".html")

  private fun File.friendly() = relativeTo(rootDir)

  companion object {
    const val CACHE_DIRNAME = "cache"
    const val RESULTS_DIRNAME = "results"

    private val DATE_FORMAT = DateTimeFormatter.ofPattern("YYYY-MM-DD").withZone(UTC)
    private val TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss").withZone(UTC)
  }
}
