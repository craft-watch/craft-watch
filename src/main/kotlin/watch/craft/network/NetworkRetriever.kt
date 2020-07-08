package watch.craft.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.engine.cio.endpoint
import io.ktor.client.features.BrowserUserAgent
import io.ktor.client.features.UserAgent
import io.ktor.client.request.get
import io.ktor.http.Url
import io.ktor.util.KtorExperimentalAPI
import mu.KotlinLogging
import java.net.URI

class NetworkRetriever : Retriever {
  private val logger = KotlinLogging.logger {}

  @OptIn(KtorExperimentalAPI::class)
  private val client = HttpClient(CIO) {
    install(UserAgent) {
      agent = "CraftWatch Bot (https://craft.watch)"
    }

    engine {
      requestTimeout = 30_000

      endpoint {
        connectTimeout = 15_000
        connectRetryAttempts = 10
      }
    }
  }

  override suspend fun retrieve(url: URI): ByteArray {
    logger.info("${url}: reading from network")
    return client.get(Url(url.toString()))
  }

  override fun close() {
    client.close()
  }
}
