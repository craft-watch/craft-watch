package watch.craft.storage

class WriteThroughObjectStore(
  private val firstLevel: ObjectStore,
  private val secondLevel: ObjectStore
) : ObjectStore {
  override fun write(key: String, content: ByteArray) {
    try {
      secondLevel.write(key, content) // Do this first, so we never end up with stuff in the primary that isn't in secondary
    } catch (e: FileExistsException) {
      // Swallow
    }
    firstLevel.write(key, content)  // Allow this to throw
  }

  override fun read(key: String): ByteArray {
    return try {
      firstLevel.read(key)
    } catch (e: FileDoesntExistException) {
      val content = secondLevel.read(key)
      firstLevel.write(key, content)
      content
    }
  }
}
