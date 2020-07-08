package watch.craft.executor

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import mu.KotlinLogging
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import watch.craft.FatalScraperException
import watch.craft.NonFatalScraperException
import watch.craft.Scraper
import watch.craft.Scraper.Job
import watch.craft.Scraper.Job.Leaf
import watch.craft.Scraper.Job.More
import watch.craft.Scraper.ScrapedItem
import watch.craft.SkipItemException
import watch.craft.network.Retriever
import java.net.URI

class ScraperAdapter(
  private val retriever: Retriever,
  private val scraper: Scraper
) {
  data class Result(
    val breweryName: String,
    val rawName: String,
    val url: URI,
    val item: ScrapedItem
  )

  private val logger = KotlinLogging.logger {}
  private val breweryName = scraper.brewery.shortName

  suspend fun execute() = scraper.jobs.executeAll()

  private suspend fun List<Job>.executeAll() = coroutineScope {
    this@executeAll
      .map { async { it.execute() } }
      .flatMap { it.await() }
  }

  private suspend fun Job.execute(): List<Result> {
    logger.info("Scraping${suffix()}: $url".prefixed())
    val doc = request(url)

    return when (this@execute) {
      is More -> processGracefully(doc, emptyList()) { work(doc) }.executeAll()
      is Leaf -> processGracefully(doc, emptyList()) {
        listOf(
          Result(
            breweryName = breweryName,
            rawName = name,
            url = url,
            item = work(doc)
          )
        )
      }
    }
  }

  private fun <R> Job.processGracefully(doc: Document, default: R, block: (Document) -> R) = try {
    block(doc)
  } catch (e: FatalScraperException) {
    throw e
  } catch (e: SkipItemException) {
    logger.info("Skipping${suffix()} because: ${e.message}".prefixed())
    default
  } catch (e: NonFatalScraperException) {
    logger.warn("${errorClause}${suffix()}".prefixed(), e)
    default
  } catch (e: Exception) {
    logger.warn("${unexpectedErrorClause}${suffix()}".prefixed(), e)
    default
  }

  private suspend fun request(url: URI) = try {
    Jsoup.parse(
      String(retriever.retrieve(url)),
      url.toString()
    )!!
  } catch (e: CancellationException) {
    throw e   // These must be propagated
  } catch (e: Exception) {
    throw FatalScraperException("Could not read page: ${url}".prefixed(), e)
  }

  private fun Job.suffix() = if (name != null) " [${name}]" else ""

  private fun String.prefixed() = "[$breweryName] ${this}"

  companion object {
    private const val errorClause = "Error while scraping"
    private const val unexpectedErrorClause = "Unexpected error while scraping"
  }
}
