package watch.craft.storage

import java.io.FileNotFoundException

class WriteThroughObjectStore(
  private val firstLevel: ObjectStore,
  private val secondLevel: ObjectStore
) : ObjectStore {
  override fun write(key: String, content: ByteArray) {
    secondLevel.write(key, content) // Do this first, so we never end up with stuff in the primary that isn't in secondary
    firstLevel.write(key, content)
  }

  override fun read(key: String): ByteArray {
    return try {
      firstLevel.read(key)
    } catch (e: FileNotFoundException) {
      val content = secondLevel.read(key)
      firstLevel.write(key, content)
      content
    }
  }
}
