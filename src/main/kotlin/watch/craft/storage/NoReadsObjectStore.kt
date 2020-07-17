package watch.craft.storage

class NoReadsObjectStore(delegate: ObjectStore) : ObjectStore by delegate {
  @Throws(FileDoesntExistException::class)
  override fun read(key: String): ByteArray = throw FileDoesntExistException(javaClass.name)
}
