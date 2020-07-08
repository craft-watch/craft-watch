package watch.craft.network

import java.net.URI

interface Retriever {
  suspend fun retrieve(url: URI): ByteArray
}
