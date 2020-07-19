package watch.craft.executor

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import mu.KotlinLogging
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import watch.craft.*
import watch.craft.Scraper.Job
import watch.craft.Scraper.Job.Leaf
import watch.craft.Scraper.Job.More
import watch.craft.Scraper.ScrapedItem
import watch.craft.dsl.selectFrom
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

    suspend fun List<Job>.executeAll() = coroutineScope {
      this@executeAll
        .map { async { it.execute() } }
        .flatMap { it.await() }
    }

    private suspend fun Job.execute(): List<Result> = when (this@execute) {
      is More -> processGracefully(url) { work(it) }.executeAll()
      is Leaf -> processGracefully(url) {
        numRawItems.incrementAndGet()
        listOf(
          Result(
            breweryId = breweryId,
            rawName = name,
            url = url,
            item = work(it)
          )
        )
      }
    }

    private suspend fun <R> Job.processGracefully(url: URI, block: (Document) -> List<R>) = try {
      logger.info("Scraping${suffix()}: $url".prefixed())
      val doc = request(url)
      block(doc)
    } catch (e: Exception) {
      when (e) {
        is FatalScraperException -> throw e
        is SkipItemException -> trackAsInfo(numSkipped, "Skipping${suffix()} because: ${e.message}")
        is MalformedInputException -> trackAsWarn(numMalformed, "Error while scraping${suffix()}", e)
        is UnretrievableException -> trackAsWarn(numUnretrievable, "Couldn't retrieve page${suffix()}", e)
        else -> trackAsWarn(numErrors, "Unexpected error while scraping${suffix()}", e)
      }
      emptyList<R>()
    }
  }

  private fun trackAsInfo(counter: AtomicInteger, msg: String) {
    counter.incrementAndGet()
    logger.info(msg.prefixed())
  }

  private fun trackAsWarn(counter: AtomicInteger, msg: String, cause: Exception) {
    counter.incrementAndGet()
    logger.warn(msg.prefixed(), cause)
  }

  private suspend fun request(url: URI) = Jsoup.parse(
    String(retriever.retrieve(url, ".html", ::validate)),
    url.toString()
  )!!

  // Enough to handle e.g. Wander Beyond serving up random Wix placeholder pages
  private fun validate(content: ByteArray) {
    try {
      Jsoup.parse(String(content)).selectFrom("title")
    } catch (e: Exception) {
      throw MalformedInputException("Can't extract <title>", e)
    }
  }

  private fun Job.suffix() = if (name != null) " [${name}]" else ""

  private fun String.prefixed() = "[$breweryId] ${this}"
}
