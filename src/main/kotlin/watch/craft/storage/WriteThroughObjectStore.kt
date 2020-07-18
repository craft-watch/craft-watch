package watch.craft.storage

class WriteThroughObjectStore(
  private val firstLevel: ObjectStore,
  private val secondLevel: ObjectStore
) : ObjectStore {
  override suspend fun write(key: String, content: ByteArray) {
    secondLevel.writeGracefully(
      key,
      content
    )  // Do this first, so we never end up with stuff in the primary that isn't in secondary
    firstLevel.write(key, content)  // Allow this to throw
  }

  override suspend fun read(key: String) = try {
    firstLevel.read(key)
  } catch (e: FileDoesntExistException) {
    val content = secondLevel.read(key)
    firstLevel.writeGracefully(key, content)  // Race conditions are anticipated due to concurrent writers
    content
  }

  // We go straight to second-level storage as source of truth
  override suspend fun list(key: String) = secondLevel.list(key)

  private suspend fun ObjectStore.writeGracefully(key: String, content: ByteArray) {
    try {
      write(key, content)
    } catch (e: FileExistsException) {
      // Swallow
    }
  }
}
