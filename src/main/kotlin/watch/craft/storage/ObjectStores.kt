package watch.craft.storage

fun ObjectStore.readOnly() = object : ObjectStore by this {
  override suspend fun write(key: String, content: ByteArray) {}
}

fun ObjectStore.resolve(relative: String) = object : ObjectStore {
  override val path = "${this@resolve.path}/${relative}"
  override suspend fun write(key: String, content: ByteArray) = this@resolve.write(fqk(key), content)
  override suspend fun read(key: String) = this@resolve.read(fqk(key))
  override suspend fun list(key: String) = this@resolve.list(fqk(key))
  private fun fqk(key: String) = "${relative}/${key}"
}

fun ObjectStore.frontedBy(front: ObjectStore) = object : ObjectStore {
  override suspend fun write(key: String, content: ByteArray) {
    this@frontedBy.writeGracefully(
      key,
      content
    )  // Do this first, so we never end up with stuff in the front storage that isn't in backing
    front.write(key, content)  // Allow this to throw
  }

  override suspend fun read(key: String) = try {
    front.read(key)
  } catch (e: FileDoesntExistException) {
    val content = this@frontedBy.read(key)
    front.writeGracefully(key, content)  // Race conditions are anticipated due to concurrent writers
    content
  }

  // We go straight to backing storage as source of truth
  override suspend fun list(key: String) = this@frontedBy.list(key)

  private suspend fun ObjectStore.writeGracefully(key: String, content: ByteArray) {
    try {
      write(key, content)
    } catch (e: FileExistsException) {
      // Swallow
    }
  }
}
