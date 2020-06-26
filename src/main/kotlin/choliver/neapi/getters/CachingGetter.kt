package choliver.neapi.getters

import choliver.neapi.sha1
import mu.KotlinLogging
import java.io.File
import java.net.URI
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

class CachingGetter(
  private val cacheDir: File,
  private val delegate: Getter<String>
): Getter<String> {
  private val logger = KotlinLogging.logger {}

  override fun request(url: URI): String {
    val hash = url.toString().sha1()
    val zip = File(cacheDir, "${hash}.zip")

    return if (zip.exists()) {
      logger.info("${url}: reading from cache: ${zip}")
      fromZip(zip)
    } else {
      logger.info("${url}: writing to cache: ${zip}")
      delegate.request(url).also { toZip(zip, it) }
    }
  }

  private fun toZip(zip: File, text: String) {
    zip.parentFile.mkdirs()
    ZipOutputStream(zip.outputStream().buffered()).use { zos ->
      zos.putNextEntry(ZipEntry("my.html"))
      zos.write(text.toByteArray())
      zos.closeEntry()
    }
  }

  private fun fromZip(zip: File): String {
    return ZipInputStream(zip.inputStream().buffered()).use { zis ->
      zis.nextEntry
      zis.reader().readText()
    }
  }
}
