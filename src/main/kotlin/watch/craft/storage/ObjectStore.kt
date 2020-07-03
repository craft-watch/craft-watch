package watch.craft.storage

interface ObjectStore {
  @Throws(FileExistsException::class)
  fun write(key: String, content: ByteArray)

  @Throws(FileDoesntExistException::class)
  fun read(key: String): ByteArray

//  /** Lists direct children. */
//  fun list(key: String): List<String>
}
