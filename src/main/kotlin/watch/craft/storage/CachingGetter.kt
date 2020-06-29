package watch.craft.storage

import mu.KotlinLogging
import watch.craft.sha1
import java.io.FileNotFoundException
import java.net.URI

class CachingGetter(
  private val store: SubObjectStore,
  private val delegate: Getter
): Getter {
  private val logger = KotlinLogging.logger {}

  override fun request(url: URI) = read(url)
    ?: delegate.request(url).also { write(url, it) }

  private fun write(url: URI, content: ByteArray) {
    val key = key(url)
    store.write(key, content)
    logger.info("${url} written to cache: ${key}")
  }

  private fun read(url: URI) = try {
    val key = key(url)
    store.read(key).also { logger.info("${url} read from cache: ${key}") }
  } catch (e: FileNotFoundException) {
    null
  }

  private fun key(url: URI) = "${url.toString().sha1()}.html" // TODO - suffix
}
