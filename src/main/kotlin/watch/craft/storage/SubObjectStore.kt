package watch.craft.storage

class SubObjectStore(
  private val delegate: ObjectStore,
  val base: String
) : ObjectStore {
  override fun write(key: String, content: ByteArray) = delegate.write(fqk(key), content)
  override fun read(key: String) = delegate.read(fqk(key))
  override fun list(key: String) = delegate.list(fqk(key))
  private fun fqk(key: String) = "${base}/${key}"
}
