package watch.craft.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import io.ktor.http.Url
import mu.KotlinLogging
import java.net.URI

class NetworkRetriever : Retriever {
  private val logger = KotlinLogging.logger {}
  private val client = HttpClient(CIO)

  override suspend fun retrieve(url: URI): ByteArray {
    logger.info("${url}: reading from network")
    return client.get(Url(url.toString()))
  }

  override fun close() {
    client.close()
  }
}
