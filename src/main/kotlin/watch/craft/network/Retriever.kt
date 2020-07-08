package watch.craft.network

import java.net.URI

interface Retriever : AutoCloseable {
  suspend fun retrieve(url: URI): ByteArray

  override fun close() {}
}
