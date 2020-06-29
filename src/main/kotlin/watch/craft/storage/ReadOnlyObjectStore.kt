package watch.craft.storage

class ReadOnlyObjectStore(private val delegate: ObjectStore) : ObjectStore by delegate {
  override fun write(key: String, content: ByteArray) {}
}
