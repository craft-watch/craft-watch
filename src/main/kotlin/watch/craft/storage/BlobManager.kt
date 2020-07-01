package watch.craft.storage

import mu.KotlinLogging
import watch.craft.sha1

class BlobManager(private val store: ObjectStore) {
  private val logger = KotlinLogging.logger {}

  fun write(content: ByteArray): String {
    val key = content.key()
    try {
      store.write(key, content)
      logger.info("Blob written: ${key}")
    } catch (e: FileExistsException) {
      // Doesn't matter for a CAM - we assume there are no malicious writers
      logger.info("Blob already present: ${key}")
    }
    return key
  }

  @Throws(FileDoesntExistException::class)
  fun read(key: String) = store.read(key).also { logger.info("Blob read: ${key}") }

  private fun ByteArray.key() = sha1()
}
