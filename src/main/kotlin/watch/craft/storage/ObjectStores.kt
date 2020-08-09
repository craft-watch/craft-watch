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

fun ObjectStore.backedBy(backer: ObjectStore) = object : ObjectStore {
  override suspend fun write(key: String, content: ByteArray) {
    backer.writeGracefully(
      key,
      content
    )  // Do this first, so we never end up with stuff in the primary that isn't in secondary
    this@backedBy.write(key, content)  // Allow this to throw
  }

  override suspend fun read(key: String) = try {
    this@backedBy.read(key)
  } catch (e: FileDoesntExistException) {
    val content = backer.read(key)
    this@backedBy.writeGracefully(key, content)  // Race conditions are anticipated due to concurrent writers
    content
  }

  // We go straight to second-level storage as source of truth
  override suspend fun list(key: String) = backer.list(key)

  private suspend fun ObjectStore.writeGracefully(key: String, content: ByteArray) {
    try {
      write(key, content)
    } catch (e: FileExistsException) {
      // Swallow
    }
  }
}
