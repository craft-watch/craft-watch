package watch.craft.storage

import mu.KotlinLogging
import watch.craft.sha1
import java.io.FileNotFoundException
import java.net.URI

class CachingGetter(private val store: SubObjectStore) {
  private val logger = KotlinLogging.logger {}

  fun request(url: URI) = read(url) ?: getFromNetwork(url).also { write(url, it) }

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

  private fun getFromNetwork(url: URI): ByteArray {
    logger.info("${url}: reading from network")
    return url.toURL().readBytes()
  }

  private fun key(url: URI) = url.toString().sha1()
}
