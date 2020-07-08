package watch.craft.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.engine.cio.endpoint
import io.ktor.client.features.UserAgent
import io.ktor.client.request.get
import io.ktor.http.Url
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import mu.KotlinLogging
import watch.craft.FatalScraperException
import watch.craft.network.NetworkRetriever.Response.*
import java.net.URI

class NetworkRetriever(private val name: String) : Retriever {
  private val logger = KotlinLogging.logger {}

  private data class Request(
    val url: URI,
    val response: CompletableDeferred<Response>
  )

  private sealed class Response {
    class Success(val content: ByteArray): Response()
    data class Failure(val cause: Exception) : Response()
  }

  private val channel = Channel<Request>()

  init {
    GlobalScope.launch {
      logger.info("[${name}] Opening client")
      createClient().use { client ->
        for (msg in channel) {
          logger.info("${msg.url}: executing network request")
          val response = try {
            Success(client.get(Url(msg.url.toString())))
          } catch (e: CancellationException) {
            throw e   // Must not swallow
          } catch (e: Exception) {
            Failure(e)
          }
          msg.response.complete(response)
        }
      }
      logger.info("[${name}] Client closed")
    }
  }

  override suspend fun retrieve(url: URI): ByteArray {
    logger.info("${url}: queueing network request")

    val msg = Request(
      url = url,
      response = CompletableDeferred()
    )

    channel.send(msg)

    when (val response = msg.response.await()) {
      is Success -> return response.content
      is Failure -> throw FatalScraperException("Error requesting ${url}", response.cause)
    }
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
