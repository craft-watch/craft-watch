package watch.craft.storage

import java.io.File
import java.io.FileNotFoundException

class LocalObjectStore(private val rootDir: File) : ObjectStore {
  override fun write(key: String, content: ByteArray) {
    // TODO - switch to NIO and use copyTo to make this atomic
    val file = resolveFile(key)
    if (file.exists()) {
      throw FileExistsException(file.toString())
    }
    file.parentFile.mkdirs()
    file.writeBytes(content)
  }

  override fun read(key: String) = try {
    resolveFile(key).readBytes()
  } catch (e: FileNotFoundException) {
    throw FileDoesntExistException(key)
  }

  override fun list(key: String): List<String> {
    TODO()
  }

  private fun resolveFile(key: String) = rootDir.resolve(key)   // TODO - make this safe
}
