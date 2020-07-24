package watch.craft.executor

import com.fasterxml.jackson.module.kotlin.readValue
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import mu.KotlinLogging
import org.jsoup.Jsoup
import watch.craft.*
import watch.craft.Scraper.*
import watch.craft.Scraper.Job.*
import watch.craft.Scraper.Work.HtmlWork
import watch.craft.Scraper.Work.JsonWork
import watch.craft.dsl.selectFrom
import watch.craft.network.Retriever
import watch.craft.utils.mapper
import java.net.URI
import java.util.concurrent.atomic.AtomicInteger

class ScraperAdapter(
  private val retriever: Retriever,
  private val scraper: Scraper,
  private val breweryId: String
) {
  data class Result(
    val breweryId: String,
    val rawName: String,
    val url: URI,
    val item: ScrapedItem
  )

  private val logger = KotlinLogging.logger {}

  suspend fun execute() = with(Context()) {
    val results = scraper.jobs.executeAll()
    StatsWith(
      results,
      BreweryStats(
        breweryId = breweryId,
        numRawItems = numRawItems.toInt(),
        numSkipped = numSkipped.toInt(),
        numMalformed = numMalformed.toInt(),
        numUnretrievable = numUnretrievable.toInt(),
        numErrors = numErrors.toInt()
      )
    )
  }

  private inner class Context {
    // TODO - may be safer to return and reduce immutable instances of BreweryStats
    val numRawItems = AtomicInteger()
    val numSkipped = AtomicInteger()
    val numMalformed = AtomicInteger()
    val numUnretrievable = AtomicInteger()
    val numErrors = AtomicInteger()

    suspend fun List<Job>.executeAll(depth: Int = 0) = coroutineScope {
      this@executeAll
        .map { async { it.execute(depth) } }
        .flatMap { it.await() }
    }

    private suspend fun Job.execute(depth: Int): List<Result> {
      return when (this@execute) {
        is More -> processGracefully(depth, work) { it }.executeAll(depth + 1)
        is Leaf -> processGracefully(depth, work) {
          numRawItems.incrementAndGet()
          listOf(
            Result(
              breweryId = breweryId,
              rawName = name,
              url = work.url,
              item = it
            )
          )
        }
      }
    }

    private suspend fun <T, R> Job.processGracefully(
      depth: Int,
      work: Work<T>,
      block: (T) -> List<R>
    ) = try {
      logger.info("Scraping${suffix()}: ${work.url}".prefixed())
      validateDepth(depth)
      block(
        when (work) {
          is JsonWork -> work.work(requestJson(work.url))
          is HtmlWork -> work.work(requestHtml(work.url))
        }
      )
    } catch (e: Exception) {
      handleException(e)
      emptyList<R>()
    }

    private fun validateDepth(depth: Int) {
      if (depth >= MAX_DEPTH) {
        throw MaxDepthExceededException("Max depth exceeded")
      }
    }

    private fun Job.handleException(e: Exception) {
      when (e) {
        is FatalScraperException -> throw e
        is SkipItemException -> trackAsInfo(numSkipped, "Skipping${suffix()} because: ${e.message}")
        is MalformedInputException -> trackAsWarn(numMalformed, "Error while scraping${suffix()}", e)
        is UnretrievableException -> trackAsWarn(numUnretrievable, "Couldn't retrieve page${suffix()}", e)
        // Rare enough that no need for dedicated counter
        is MaxDepthExceededException -> trackAsWarn(numErrors, "Max depth exceeded while scraping${suffix()}", e)
        else -> trackAsWarn(numErrors, "Unexpected error while scraping${suffix()}", e)
      }
    }
  }

  private fun trackAsInfo(counter: AtomicInteger, msg: String) {
    counter.incrementAndGet()
    logger.info(msg.prefixed())
  }

  private fun trackAsWarn(counter: AtomicInteger, msg: String, cause: Exception? = null) {
    counter.incrementAndGet()
    logger.warn(msg.prefixed(), cause)
  }

  private suspend fun requestJson(url: URI) = mapper().readValue<Any>(
    String(retriever.retrieve(url, ".json") { Unit })  // TODO - validation
  )

  private suspend fun requestHtml(url: URI) = Jsoup.parse(
    String(retriever.retrieve(url, ".html", ::validateHtml)),
    url.toString()
  )!!

  // Enough to handle e.g. Wander Beyond serving up random Wix placeholder pages
  private fun validateHtml(content: ByteArray) {
    try {
      Jsoup.parse(String(content)).selectFrom("title")
    } catch (e: Exception) {
      throw MalformedInputException("Can't extract <title>", e)
    }
  }

  private fun Job.suffix() = if (name != null) " [${name}]" else ""

  private fun String.prefixed() = "[$breweryId] ${this}"

  companion object {
    private const val MAX_DEPTH = 20    // Pretty arbitrary - assume no more than this # of paginated results
  }
}
