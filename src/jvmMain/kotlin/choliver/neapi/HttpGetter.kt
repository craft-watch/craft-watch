package choliver.neapi

import mu.KotlinLogging
import java.io.File
import java.net.URI
import java.security.MessageDigest

class HttpGetter(private val cacheDir: File) {
  private val logger = KotlinLogging.logger {}

  fun get(url: URI): String {
    // TODO - logging
    val hash = url.toString().sha1()
    val file = File(cacheDir, "${hash}.html")

    return if (file.exists()) {
      logger.info("${url}: reading from cache: ${file}")
      file.readText()
    } else {
      logger.info("${url}: writing to cache: ${file}")
      val text = url.toURL().readText()
      file.parentFile.mkdirs()
      file.writeText(text)
      text
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
