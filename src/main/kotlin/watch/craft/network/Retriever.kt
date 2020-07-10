package watch.craft.network

import java.net.URI

interface Retriever : AutoCloseable {
  suspend fun retrieve(url: URI, suffix: String? = null): ByteArray

  override fun close() {}
}
