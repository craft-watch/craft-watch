package watch.craft.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import mu.KotlinLogging
import java.net.URI

class NetworkRetriever : Retriever {
  private val logger = KotlinLogging.logger {}

  override suspend fun retrieve(url: URI): ByteArray = HttpClient(CIO).use { client ->
    logger.info("${url}: reading from network")
    client.get(url.toURL())
  }
}
