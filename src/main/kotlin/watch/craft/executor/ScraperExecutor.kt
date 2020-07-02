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

class ScraperExecutor(
  private val getter: CachingGetter,
  private val scraper: Scraper
) {
  data class Result(
    val brewery: String,
    val entry: IndexEntry,
    val item: ScrapedItem
  )

  private val logger = KotlinLogging.logger {}
  private val brewery = scraper.name

  fun execute(): List<Result> {
    val entries = scraper.rootUrls
      .parallelStream()
      .map { scrapeIndexSafely(scraper, it) }
      .collect(Collectors.toList())
      .flatten()

    return entries
      .parallelStream()
      .map { entry -> scrapeItemSafely(entry)?.let { Result(brewery, entry, it) } }
      .collect(Collectors.toList())
      .filterNotNull()
  }

  private fun scrapeIndexSafely(scraper: Scraper, url: URI): List<IndexEntry> {
    logger.info("[${brewery}] Scraping index: ${url}")
    return try {
      scraper.scrapeIndex(request(url))
    } catch (e: NonFatalScraperException) {
      logger.warn("[${brewery}] Error while scraping brewery", e)
      emptyList()
    } catch (e: FatalScraperException) {
      logger.error("[${brewery}] Fatal error while scraping brewery", e)
      throw e
    } catch (e: Exception) {
      logger.warn("[${brewery}] Unexpected error while scraping brewery", e)
      emptyList()
    }
  }

  private fun scrapeItemSafely(entry: IndexEntry): ScrapedItem? {
    logger.info("[${brewery}] Scraping [${entry.rawName}]")
    return try {
      entry.scrapeItem(request(entry.url))
    } catch (e: SkipItemException) {
      logger.info("[${brewery}] Skipping [${entry.rawName}] because: ${e.message}")
      null
    } catch (e: NonFatalScraperException) {
      logger.warn("[${brewery}] Error while scraping [${entry.rawName}]", e)
      null
    } catch (e: FatalScraperException) {
      logger.error("[${brewery}] Error while scraping [${entry.rawName}]", e)
      throw e
    } catch (e: Exception) {
      logger.warn("[${brewery}] Unexpected error while scraping [${entry.rawName}]", e)
      null
    }
  }

  private fun request(url: URI) = try {
    Jsoup.parse(String(getter.request(url)), url.toString())!!
  } catch (e: Exception) {
    throw FatalScraperException("Could not read page: ${url}", e)
  }
}
