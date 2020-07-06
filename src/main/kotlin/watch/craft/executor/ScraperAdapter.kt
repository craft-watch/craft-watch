package watch.craft.executor

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import mu.KotlinLogging
import org.jsoup.Jsoup
import watch.craft.FatalScraperException
import watch.craft.NonFatalScraperException
import watch.craft.Scraper
import watch.craft.Scraper.Job
import watch.craft.Scraper.Job.Leaf
import watch.craft.Scraper.Job.More
import watch.craft.Scraper.ScrapedItem
import watch.craft.SkipItemException
import watch.craft.storage.CachingGetter
import java.net.URI

class ScraperAdapter(
  private val getter: CachingGetter,
  private val scraper: Scraper,
  private val rateLimitPeriodMillis: Int = 10
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
      .map { job -> async { job.execute() } }
      .flatMap { it.await() }
  }

  // TODO - rate-limiting
  private suspend fun Job.execute(): List<Result> = when (this@execute) {
    is More -> execute()
    is Leaf -> execute()
  }

  private suspend fun More.execute(): List<Result> {
    logger.info("Scraping: $url".prefixed())
    val doc = request(url)

    val children: List<Job> = try {
      work(doc)
    } catch (e: FatalScraperException) {
      throw e
    } catch (e: NonFatalScraperException) {
      logger.warn("${errorClause}: $url".prefixed(), e)
      emptyList()
    } catch (e: Exception) {
      logger.warn("${unexpectedErrorClause}: $url".prefixed(), e)
      emptyList()
    }

    return children.executeAll()
  }

  private suspend fun Leaf.execute(): List<Result> {
    logger.info("Scraping leaf [$rawName]: $url".prefixed())
    val doc = request(url)

    return try {
      listOf(
        Result(
          breweryName = breweryName,
          rawName = rawName,
          url = url,
          item = this.work(doc)
        )
      )
    } catch (e: FatalScraperException) {
      throw e
    } catch (e: SkipItemException) {
      logger.info("Skipping leaf [$rawName] because: ${e.message}".prefixed())
      emptyList()
    } catch (e: NonFatalScraperException) {
      logger.warn("${errorClause} leaf [$rawName]".prefixed(), e)
      emptyList()
    } catch (e: Exception) {
      logger.warn("${unexpectedErrorClause} leaf [$rawName]".prefixed(), e)
      emptyList()
    }
  }

  private suspend fun request(url: URI) = try {
    Jsoup.parse(
      String(
        onIoThread { getter.request(url) }
      ),
      url.toString()
    )!!
  } catch (e: Exception) {
    throw FatalScraperException("Could not read page: ${url}".prefixed(), e)
  }

  private fun String.prefixed() = "[$breweryName] ${this}"

  companion object {
    private const val errorClause = "Error while scraping"
    private const val unexpectedErrorClause = "Unexpected error while scraping"
  }
}
