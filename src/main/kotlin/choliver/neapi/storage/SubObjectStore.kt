package choliver.neapi.storage

class SubObjectStore(
  private val base: String,
  private val delegate: ObjectStore
) : ObjectStore {
  override fun write(key: String, content: ByteArray) = delegate.write(fqk(key), content)
  override fun read(key: String) = delegate.read(fqk(key))
  private fun fqk(key: String) = "${base}/${key}"
}
