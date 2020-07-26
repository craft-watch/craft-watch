package watch.craft.executor

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import mu.KotlinLogging
import watch.craft.*
import watch.craft.Scraper.Node
import watch.craft.Scraper.Node.Retrieval
import watch.craft.Scraper.Node.Retrieval.RetrievalContext
import watch.craft.Scraper.Node.ScrapedItem
import watch.craft.network.Retriever
import java.net.URI
import java.util.concurrent.atomic.AtomicInteger

class ScraperAdapter(
  private val retriever: Retriever,
  private val scraper: Scraper,
  private val breweryId: String
) {
  data class Result(
    val breweryId: String,
    val sourceUrl: URI,
    val item: ScrapedItem
  )

  private val logger = KotlinLogging.logger {}

  suspend fun execute() = with(Context()) {
    val results = process(scraper.roots)
    StatsWith(
      results,
      BreweryStats(
        breweryId = breweryId,
        numScraped = numRawItems.toInt(),
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

  private suspend fun Context.process(nodes: List<Node>) = coroutineScope {
    nodes
      .map { async { process(it) } }
      .flatMap { it.await() }
  }

  private suspend fun Context.process(node: Node) = when (node) {
    is ScrapedItem -> processScrapedItem(node)
    is Retrieval -> processWork(node)
  }

  private fun Context.processScrapedItem(scrapedItem: ScrapedItem): List<Result> {
    numRawItems.incrementAndGet()
    return listOf(
      Result(
        breweryId = breweryId,
        sourceUrl = sourceUrl!!, // TODO - avoid the "!!"
        item = scrapedItem
      )
    )
  }

  private suspend fun Context.processWork(retrieval: Retrieval): List<Result> {
    with(retrieval) {
      val nodes = try {
        logger.info("Scraping${suffix()}: ${url}".prefixed())
        validateDepth()   // TODO - put this somewhere more sensible?

        // TODO - make lazy
        val data = retriever.retrieve(url, suffix, validate)
        val context = object : RetrievalContext {
          override val data = data
        }

        context.block()
      } catch (e: Exception) {
        handleException(retrieval, e)
        return emptyList()
      }

      return sourcedAt(url).process(nodes)
    }
  }

  private fun Context.validateDepth() {
    if (depth >= MAX_DEPTH) {
      throw MaxDepthExceededException("Max depth exceeded")
    }
  }

  private fun Context.handleException(retrieval: Retrieval, e: Exception) {
    val suffix = retrieval.suffix()
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

  private fun Retrieval.suffix() = if (name != null) " [${name}]" else ""

  private fun String.prefixed() = "[$breweryId] ${this}"

  companion object {
    private const val MAX_DEPTH = 20    // Pretty arbitrary - assume no more than this # of paginated results
  }
}
