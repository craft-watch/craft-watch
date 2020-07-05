package watch.craft.executor

import mu.KotlinLogging
import org.jsoup.Jsoup
import watch.craft.FatalScraperException
import watch.craft.NonFatalScraperException
import watch.craft.Scraper
import watch.craft.Scraper.IndexEntry
import watch.craft.Scraper.ScrapedItem
import watch.craft.SkipItemException
import watch.craft.storage.CachingGetter
import java.net.URI
import java.util.stream.Collectors

class ScraperAdapter(
  private val getter: CachingGetter,
  private val scraper: Scraper
) {
  data class Result(
    val breweryName: String,
    val entry: IndexEntry,
    val item: ScrapedItem
  )

  private val logger = KotlinLogging.logger {}
  private val breweryName = scraper.brewery.shortName

  val indexTasks = scraper.rootUrls.map { url ->
    {
      scrapeIndexSafely(url).map { entry ->
        { scrapeItemSafely(entry)?.let { Result(breweryName, entry, it) } }
      }
    }
  }

  fun execute(): List<Result> {
    val entries = scraper.rootUrls
      .parallelMap { scrapeIndexSafely(it) }
      .flatten()

    return entries
      .parallelMap { entry -> scrapeItemSafely(entry)?.let { Result(breweryName, entry, it) } }
      .filterNotNull()
  }

  private fun <T, R> List<T>.parallelMap(transform: (T) -> R): List<R> = parallelStream()
    .map(transform)
    .collect(Collectors.toList())

  private fun scrapeIndexSafely(url: URI): List<IndexEntry> {
    logger.info("[${breweryName}] Scraping index: ${url}")
    return try {
      scraper.scrapeIndex(request(url))
    } catch (e: NonFatalScraperException) {
      logger.warn("[${breweryName}] Error while scraping brewery", e)
      emptyList()
    } catch (e: FatalScraperException) {
      logger.error("[${breweryName}] Fatal error while scraping brewery", e)
      throw e
    } catch (e: Exception) {
      logger.warn("[${breweryName}] Unexpected error while scraping brewery", e)
      emptyList()
    }
  }

  private fun scrapeItemSafely(entry: IndexEntry): ScrapedItem? {
    logger.info("[${breweryName}] Scraping [${entry.rawName}]")
    return try {
      entry.scrapeItem(request(entry.url))
    } catch (e: SkipItemException) {
      logger.info("[${breweryName}] Skipping [${entry.rawName}] because: ${e.message}")
      null
    } catch (e: NonFatalScraperException) {
      logger.warn("[${breweryName}] Error while scraping [${entry.rawName}]", e)
      null
    } catch (e: FatalScraperException) {
      logger.error("[${breweryName}] Error while scraping [${entry.rawName}]", e)
      throw e
    } catch (e: Exception) {
      logger.warn("[${breweryName}] Unexpected error while scraping [${entry.rawName}]", e)
      null
    }
  }

  private fun request(url: URI) = try {
    Jsoup.parse(String(getter.request(url)), url.toString())!!
  } catch (e: Exception) {
    throw FatalScraperException("Could not read page: ${url}", e)
  }
}
