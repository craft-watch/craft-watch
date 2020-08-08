package watch.craft.network

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import mu.KotlinLogging
import watch.craft.MalformedInputException
import watch.craft.UnretrievableException
import watch.craft.executor.onIoThread
import watch.craft.network.NetworkRetriever.Response.Failure
import watch.craft.network.NetworkRetriever.Response.Success
import java.io.IOException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse.BodyHandlers

class NetworkRetriever(private val config: Config) : Retriever {
  private val logger = KotlinLogging.logger {}

  data class Config(
    val id: String,
    val rateLimitPeriodMillis: Int = RATE_LIMIT_PERIOD_MILLIS,
    val failOn404: Boolean = true
  )

  private data class Request(
    val url: URI,
    val validate: (ByteArray) -> Unit,
    val response: CompletableDeferred<Response>
  )

  private sealed class Response {
    class Success(val content: ByteArray) : Response()
    data class Failure(val cause: Exception) : Response()
  }

  private val client = HttpClient.newBuilder().build()
  private val channel = Channel<Request>()

  // Manually limit concurrency by having a single coroutine handling requests over a channel.
  init {
    GlobalScope.launch {
      logger.info("[${config.id}] Creating retriever message loop")
      for (msg in channel) {
        msg.response.complete(process(msg))
      }
      logger.info("[${config.id}] Closed retriever message loop")
    }
  }

  private suspend fun process(msg: Request): Response {
    logger.info("${msg.url}: processing network request")
    var exception: Exception? = null
    repeat(MAX_RETRIES) {
      val response = try {
        val request = HttpRequest.newBuilder()
          .uri(msg.url)
          .setHeader("User-Agent", USER_AGENT)
          .build()
        val response = onIoThread { client.send(request, BodyHandlers.ofByteArray()) }
        if (response.statusCode() in 200..299 || (!config.failOn404 && response.statusCode() == 404)) {
          msg.validate(response.body())
          Success(response.body())
        } else {
          Failure(RuntimeException("Response status code: ${response.statusCode()}"))
        }
      } catch (e: IOException) {
        exception = e
        null
      } catch (e: MalformedInputException) {
        exception = e
        null
      } catch (e: CancellationException) {
        throw e   // Must not swallow
      } catch (e: Exception) {
        Failure(e)  // No idea what other exceptions Ktor throws, so surface as an immediate failure
      }

      delay(config.rateLimitPeriodMillis.toLong())
      if (response != null) {
        return response
      }
    }
    return Failure(exception!!)
  }

  override suspend fun retrieve(
    url: URI,
    suffix: String?,
    validate: (ByteArray) -> Unit
  ): ByteArray {
    logger.info("${url}: queueing network request")

    val msg = Request(
      url = url,
      validate = validate,
      response = CompletableDeferred()
    )

    channel.send(msg)

    when (val response = msg.response.await()) {
      is Success -> return response.content
      is Failure -> throw UnretrievableException("Error retrieving ${url}", response.cause)
    }
  }

  override fun close() {
    channel.close()
  }

  companion object {
    private const val USER_AGENT = "CraftWatch Bot (https://craft.watch)"
    private const val RATE_LIMIT_PERIOD_MILLIS = 3000
    private const val MAX_RETRIES = 5
  }
}
