package watch.craft.executor

import com.fasterxml.jackson.module.kotlin.readValue
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import mu.KotlinLogging
import org.jsoup.Jsoup
import watch.craft.*
import watch.craft.Scraper.Output
import watch.craft.Scraper.Output.*
import watch.craft.Scraper.Output.Work.*
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
    val url: URI,
    val item: ScrapedItem
  )

  private val logger = KotlinLogging.logger {}

  suspend fun execute() = with(Context()) {
    val results = process(scraper.seed)
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

  private data class Context(
    val depth: Int = 0,
    val sourceUrl: URI? = null,
    // TODO - may be safer to return and reduce immutable instances of BreweryStats
    val numRawItems: AtomicInteger = AtomicInteger(),
    val numSkipped: AtomicInteger = AtomicInteger(),
    val numMalformed: AtomicInteger = AtomicInteger(),
    val numUnretrievable: AtomicInteger = AtomicInteger(),
    val numErrors: AtomicInteger = AtomicInteger()
  ) {
    fun sourcedAt(url: URI) = copy(sourceUrl = url, depth = depth + 1)
  }

  private suspend fun Context.process(output: Output): List<Result> {
    return when (output) {
      is ScrapedItem -> processScrapedItem(output)
      is Multiple -> processMultiple(output)
      is Work -> processWork(output)
    }
  }

  private fun Context.processScrapedItem(scrapedItem: ScrapedItem): List<Result> {
    numRawItems.incrementAndGet() // TODO - this needs to move to *before* we do the work

    return listOf(
      Result(
        breweryId = breweryId,
        url = sourceUrl!!, // TODO - avoid the "!!"
        item = scrapedItem
      )
    )
  }

  private suspend fun Context.processMultiple(multiple: Multiple) = coroutineScope {
    multiple.outputs
      .map { async { process(it) } }
      .flatMap { it.await() }
  }

  private suspend fun Context.processWork(work: Work) = executeWork(work)
    ?.let { sourcedAt(work.url).process(it) }
    ?: emptyList()

  private suspend fun Context.executeWork(work: Work) = try {
    logger.info("Scraping${work.suffix()}: ${work.url}".prefixed())
    validateDepth()
    when (work) {
      is JsonWork -> work.block(requestJson(work.url))
      is HtmlWork -> work.block(requestHtml(work.url))
    }
  } catch (e: Exception) {
    handleException(work, e)
    null
  }

  private fun Context.validateDepth() {
    if (depth >= MAX_DEPTH) {
      throw MaxDepthExceededException("Max depth exceeded")
    }
  }

  private fun Context.handleException(work: Work, e: Exception) {
    val suffix = work.suffix()
    when (e) {
      is FatalScraperException -> throw e
      is SkipItemException -> trackAsInfo(numSkipped, "Skipping${suffix} because: ${e.message}")
      is MalformedInputException -> trackAsWarn(numMalformed, "Error while scraping${suffix}", e)
      is UnretrievableException -> trackAsWarn(numUnretrievable, "Couldn't retrieve page${suffix}", e)
      // Rare enough that no need for dedicated counter
      is MaxDepthExceededException -> trackAsWarn(numErrors, "Max depth exceeded while scraping${suffix}", e)
      else -> trackAsWarn(numErrors, "Unexpected error while scraping${suffix}", e)
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

  private fun Work.suffix() = if (name != null) " [${name}]" else ""

  private fun String.prefixed() = "[$breweryId] ${this}"

  companion object {
    private const val MAX_DEPTH = 20    // Pretty arbitrary - assume no more than this # of paginated results
  }
}
