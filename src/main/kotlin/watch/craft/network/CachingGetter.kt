package watch.craft.network

import mu.KotlinLogging
import watch.craft.storage.FileDoesntExistException
import watch.craft.storage.FileExistsException
import watch.craft.storage.SubObjectStore
import watch.craft.utils.sha1
import java.net.URI

class CachingGetter(
  private val store: SubObjectStore,
  private val networkGet: (URI) -> ByteArray = { it.toURL().readBytes() }
) {
  private val logger = KotlinLogging.logger {}

  fun request(url: URI) = read(url) ?: getFromNetwork(url).also { write(url, it) }

  private fun write(url: URI, content: ByteArray) {
    val key = key(url)
    try {
      store.write(key, content)
      logger.info("${url} written to cache: ${key}")
    } catch (e: FileExistsException) {
      // Another writer raced us to write to this location in the cache
    }
  }

  private fun read(url: URI) = try {
    val key = key(url)
    store.read(key).also { logger.info("${url} read from cache: ${key}") }
  } catch (e: FileDoesntExistException) {
    null
  }

  private fun getFromNetwork(url: URI): ByteArray {
    logger.info("${url}: reading from network")
    return networkGet(url)
  }

  private fun key(url: URI) = url.toString().sha1()
}
