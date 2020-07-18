package watch.craft.network

import watch.craft.MalformedInputException
import java.net.URI

interface Retriever : AutoCloseable {
  suspend fun retrieve(
    url: URI,
    suffix: String? = null,
    /** Signals failure by throwing [MalformedInputException]. */
    validate: (ByteArray) -> Unit
  ): ByteArray

  override fun close() {}
}
