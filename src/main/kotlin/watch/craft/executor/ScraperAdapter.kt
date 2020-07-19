package watch.craft.executor

import kotlinx.coroutines.CancellationException
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

    private suspend fun Job.execute(): List<Result> {
      logger.info("Scraping${suffix()}: $url".prefixed())
      return when (this@execute) {
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
    }

    private suspend fun <R> Job.processGracefully(url: URI, block: (Document) -> List<R>) = try {
      val doc = request(url)
      block(doc)
    } catch (e: Exception) {
      val (counter, msg) = when (e) {
        is FatalScraperException -> throw e
        is SkipItemException -> numSkipped to "Skipping${suffix()} because: ${e.message}"
        is MalformedInputException -> numMalformed to "Error while scraping${suffix()}"
        is UnretrievableException -> numUnretrievable to "Couldn't retrieve page${suffix()}"
        else -> numErrors to "Unexpected error while scraping${suffix()}"
      }
      counter.incrementAndGet()
      logger.info(msg.prefixed())
      emptyList<R>()
    }
  }

  private suspend fun request(url: URI) = try {
    Jsoup.parse(
      String(retriever.retrieve(url, ".html", ::validate)),
      url.toString()
    )!!
  } catch (e: CancellationException) {
    throw e   // These must be propagated
  } catch (e: Exception) {
    throw FatalScraperException("Could not read page: ${url}".prefixed(), e)
  }

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
