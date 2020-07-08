package watch.craft.storage

class NullObjectStore : ObjectStore {
  override fun write(key: String, content: ByteArray) {}

  override fun read(key: String): ByteArray {
    throw FileDoesntExistException("Null object store")
  }
}
