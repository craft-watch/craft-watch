package watch.craft.storage

class SubObjectStore(
  private val delegate: ObjectStore,
  private val base: String
) : ObjectStore {
  override val path = "${delegate.path}/${base}"
  override suspend fun write(key: String, content: ByteArray) = delegate.write(fqk(key), content)
  override suspend fun read(key: String) = delegate.read(fqk(key))
  override suspend fun list(key: String) = delegate.list(fqk(key))
  private fun fqk(key: String) = "${base}/${key}"
}
