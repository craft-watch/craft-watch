package watch.craft.getters

import mu.KotlinLogging
import watch.craft.sha1
import watch.craft.storage.SubObjectStore
import java.io.FileNotFoundException
import java.net.URI

class CachingGetter(
  private val store: SubObjectStore,
  private val delegate: Getter<String>
): Getter<String> {
  private val logger = KotlinLogging.logger {}

  override fun request(url: URI) = read(url)
    ?: delegate.request(url).also { write(url, it) }

  private fun write(url: URI, text: String) {
    val key = key(url)
    store.write(key, text.toByteArray())
    logger.info("${url} written to cache: ${key}")
  }

  private fun read(url: URI) = try {
    val key = key(url)
    String(store.read(key))
      .also { logger.info("${url} read from cache: ${key}") }
  } catch (e: FileNotFoundException) {
    null
  }

  private fun key(url: URI) = "${url.toString().sha1()}.html"
}
