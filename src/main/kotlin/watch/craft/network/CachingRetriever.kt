package watch.craft.network

import mu.KotlinLogging
import watch.craft.storage.FileDoesntExistException
import watch.craft.storage.FileExistsException
import watch.craft.storage.ObjectStore
import watch.craft.utils.sha1
import java.lang.Thread.currentThread
import java.net.URI

class CachingRetriever(
  private val store: ObjectStore,
  private val delegate: Retriever
) : Retriever {
  private val logger = KotlinLogging.logger {}

  override suspend fun retrieve(
    url: URI,
    suffix: String?,
    validate: (ByteArray) -> Unit
  ): ByteArray {
    val key = key(url, suffix)
    return read(url, key) ?: delegate.retrieve(url, suffix, validate).also { write(url, key, it) }
  }

  override fun close() = delegate.close()

  private suspend fun write(url: URI, key: String, content: ByteArray) {
    try {
      logger.info("${url} writing to cache on thread: ${currentThread().name}")
      store.write(key, content)
      logger.info("${url} written to cache: ${key}")
    } catch (e: FileExistsException) {
      // Another writer raced us to write to this location in the cache
    }
  }

  private suspend fun read(url: URI, key: String) = try {
    logger.info("${url} reading from cache on thread: ${currentThread().name}")
    val content = store.read(key)
    logger.info("${url} read from cache: ${key}")
    content
  } catch (e: FileDoesntExistException) {
    null
  }

  private fun key(url: URI, suffix: String?) = url.toString().sha1() + (suffix ?: "")
}
