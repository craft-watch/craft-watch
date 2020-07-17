package watch.craft.storage

interface ObjectStore {
  val path: String get() = ""

  @Throws(FileExistsException::class)
  suspend fun write(key: String, content: ByteArray)

  @Throws(FileDoesntExistException::class)
  suspend fun read(key: String): ByteArray

  /** Lists direct children. */
  suspend fun list(key: String = ""): List<String> = emptyList()
}
