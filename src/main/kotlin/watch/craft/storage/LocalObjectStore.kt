package watch.craft.storage

import java.io.File

class LocalObjectStore(private val rootDir: File) : ObjectStore {
  override fun write(key: String, content: ByteArray) {
    val file = resolveFile(key)
    file.parentFile.mkdirs()
    file.writeBytes(content)
  }

  override fun read(key: String) = resolveFile(key).readBytes()

  private fun resolveFile(key: String) = rootDir.resolve(key)   // TODO - make this safe
}
