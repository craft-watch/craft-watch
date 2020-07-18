package watch.craft.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.client.features.UserAgent
import io.ktor.client.request.get
import io.ktor.http.Url
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import mu.KotlinLogging
import watch.craft.FatalScraperException
import watch.craft.network.NetworkRetriever.Response.Failure
import watch.craft.network.NetworkRetriever.Response.Success
import java.io.IOException
import java.net.URI

class NetworkRetriever(
  private val id: String,
  private val rateLimitPeriodMillis: Int = RATE_LIMIT_PERIOD_MILLIS
) : Retriever {
  private val logger = KotlinLogging.logger {}

  private data class Request(
    val url: URI,
    val response: CompletableDeferred<Response>
  )

  private sealed class Response {
    class Success(val content: ByteArray) : Response()
    data class Failure(val cause: Exception) : Response()
  }

  private val channel = Channel<Request>()

  // I don't trust Ktor's "concurrency" configuration, so manually limit concurrency by having a single
  // coroutine handling requests over a channel.
  init {
    GlobalScope.launch {
      logger.info("[${id}] Opening client")
      createClient().use { client ->
        for (msg in channel) {
          msg.response.complete(client.process(msg))
        }
      }
      logger.info("[${id}] Client closed")
    }
  }

  private suspend fun HttpClient.process(msg: Request): Response {
    logger.info("${msg.url}: processing network request")
    var exception: IOException? = null
    repeat(MAX_RETRIES) {
      val response = try {
        Success(get(Url(msg.url.toString())))
      } catch (e: IOException) {
        exception = e
        null
      } catch (e: CancellationException) {
        throw e   // Must not swallow
      } catch (e: Exception) {
        Failure(e)  // No idea what other exceptions Ktor throws, so surface as an immediate failure
      }

      delay(rateLimitPeriodMillis.toLong())
      if (response != null) {
        return response
      }
    }
    return Failure(exception!!)
  }

  override suspend fun retrieve(url: URI, suffix: String?): ByteArray {
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
  private fun createClient() = HttpClient(Apache) {
    install(UserAgent) {
      agent = "CraftWatch Bot (https://craft.watch)"
    }
  }

  companion object {
    private const val RATE_LIMIT_PERIOD_MILLIS = 3000
    private const val MAX_RETRIES = 5
  }
}
