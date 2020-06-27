package watch.craft.storage

import java.io.FileNotFoundException

interface ObjectStore {
  fun write(key: String, content: ByteArray)

  @Throws(FileNotFoundException::class)
  fun read(key: String): ByteArray
}
