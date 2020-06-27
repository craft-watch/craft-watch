package choliver.neapi.storage

import java.io.FileNotFoundException

interface Backer {
  fun write(key: String, content: ByteArray)

  @Throws(FileNotFoundException::class)
  fun read(key: String): ByteArray
}
