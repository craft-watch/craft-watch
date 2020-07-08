package watch.craft.network

import mu.KotlinLogging
import watch.craft.executor.onIoThread
import watch.craft.storage.FileDoesntExistException
import watch.craft.storage.FileExistsException
import watch.craft.storage.ObjectStore
import watch.craft.utils.sha1
import java.net.URI

class CachingRetriever(
  private val store: ObjectStore,
  private val delegate: Retriever = NetworkRetriever()
) : Retriever {
  private val logger = KotlinLogging.logger {}

  override suspend fun retrieve(url: URI) = read(url) ?: delegate.retrieve(url).also { write(url, it) }

  private suspend fun write(url: URI, content: ByteArray) {
    val key = key(url)
    try {
      onIoThread { store.write(key, content) }
      logger.info("${url} written to cache: ${key}")
    } catch (e: FileExistsException) {
      // Another writer raced us to write to this location in the cache
    }
  }

  private suspend fun read(url: URI) = try {
    val key = key(url)
    onIoThread { store.read(key) }
      .also { logger.info("${url} read from cache: ${key}") }
  } catch (e: FileDoesntExistException) {
    null
  }

  private fun key(url: URI) = url.toString().sha1()
}
