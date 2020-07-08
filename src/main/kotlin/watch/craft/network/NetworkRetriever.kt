package watch.craft.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.engine.cio.endpoint
import io.ktor.client.features.UserAgent
import io.ktor.client.request.get
import io.ktor.http.Url
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import mu.KotlinLogging
import java.net.URI

class NetworkRetriever : Retriever {
  private val logger = KotlinLogging.logger {}

  private data class Request(
    val url: URI,
    val response: CompletableDeferred<ByteArray>
  )

  private val channel = Channel<Request>()

  // TODO - error-handling
  init {
    GlobalScope.launch {
      createClient().use { client ->
        for (msg in channel) {
          logger.info("${msg.url}: executing network request")
          val response: ByteArray = client.get(Url(msg.url.toString()))
          msg.response.complete(response)
        }
      }
    }
  }

  override suspend fun retrieve(url: URI): ByteArray {
    logger.info("${url}: queueing network request")
    val msg = Request(
      url = url,
      response = CompletableDeferred()
    )
    channel.send(msg)
    return msg.response.await()
  }

  override fun close() {
    channel.close()
  }

  @OptIn(KtorExperimentalAPI::class)
  private fun createClient() = HttpClient(CIO) {
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
}
