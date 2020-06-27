package choliver.neapi.storage

import java.io.File

class LocalBacker(private val rootDir: File) : Backer {
  override val desc = "local"

  override fun write(key: String, content: ByteArray) {
    val file = resolveFile(key)
    file.parentFile.mkdirs()
    file.writeBytes(content)
  }

  override fun read(key: String) = resolveFile(key).readBytes()

  private fun resolveFile(key: String) = rootDir.resolve(key)   // TODO - make this safe
}
