package choliver.neapi

import mu.KotlinLogging
import java.io.File
import java.net.URI
import java.security.MessageDigest
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

class HttpGetter(private val cacheDir: File) {
  private val logger = KotlinLogging.logger {}

  fun get(url: URI): String {
    val hash = url.toString().sha1()
    val zip = File(cacheDir, "${hash}.zip")

    return if (zip.exists()) {
      logger.info("${url}: reading from cache: ${zip}")
      fromZip(zip)
    } else {
      logger.info("${url}: writing to cache: ${zip}")
      url.toURL().readText().also { toZip(zip, it) }
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

  private fun String.sha1() = this.toByteArray().sha1()

  private fun ByteArray.sha1(): String {
    val md = MessageDigest.getInstance("SHA-1")
    md.update(this)
    return md.digest().hex()
  }

  private fun ByteArray.hex() = joinToString("") { "%02X".format(it) }
}
