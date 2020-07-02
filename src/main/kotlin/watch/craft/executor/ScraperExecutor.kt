package watch.craft.executor

import mu.KotlinLogging
import org.jsoup.Jsoup
import watch.craft.*
import watch.craft.Scraper.IndexEntry
import watch.craft.storage.CachingGetter
import java.net.URI
import java.util.stream.Collectors

class ScraperExecutor(
  private val getter: CachingGetter,
  private val scraper: Scraper
) {
  private val logger = KotlinLogging.logger {}
  private val brewery = scraper.name

  fun execute(): List<Item> {
    val entries = scraper.rootUrls
      .parallelStream()
      .map { scrapeIndexSafely(scraper, it) }
      .collect(Collectors.toList())
      .flatten()

    return entries
      .parallelStream()
      .map { scrapeItemSafely(it) }
      .collect(Collectors.toList())
      .filterNotNull()
  }

  private fun scrapeIndexSafely(scraper: Scraper, url: URI): List<IndexEntry> {
    logger.info("[${brewery}] Scraping index: ${url}")
    return try {
      scraper.scrapeIndex(request(url))
    } catch (e: NonFatalScraperException) {
      logger.warn("[${brewery}] Error scraping brewery", e)
      emptyList()
    } catch (e: FatalScraperException) {
      logger.error("[${brewery}] Fatal error scraping brewery", e)
      throw e
    } catch (e: Exception) {
      logger.warn("[${brewery}] Unexpected error scraping brewery", e)
      emptyList()
    }
  }

  private fun scrapeItemSafely(entry: IndexEntry): Item? {
    logger.info("[${brewery}] Scraping [${entry.rawName}]")
    return try {
      entry
        .scrapeItem(request(entry.url))
        .normalise(brewery, entry.url)
    } catch (e: SkipItemException) {
      logger.info("[${brewery}] Skipping [${entry.rawName}] because: ${e.message}")
      null
    } catch (e: NonFatalScraperException) {
      logger.warn("[${brewery}] Error scraping [${entry.rawName}]", e)
      null
    } catch (e: FatalScraperException) {
      logger.error("[${brewery}] Error scraping [${entry.rawName}]", e)
      throw e
    } catch (e: Exception) {
      logger.warn("[${brewery}] Unexpected error scraping [${entry.rawName}]", e)
      null
    }
  }

  private fun request(url: URI) = try {
    Jsoup.parse(String(getter.request(url)), url.toString())!!
  } catch (e: Exception) {
    throw FatalScraperException("Could not read page: ${url}", e)
  }
}
