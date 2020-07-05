package watch.craft.executor

import watch.craft.Scraper
import watch.craft.executor.ScraperAdapter.Result
import watch.craft.storage.CachingGetter
import java.util.concurrent.Executors.newFixedThreadPool

class ConcurrentRawScraperExecutor(
  private val concurrency: Int = 4,
  private val getter: CachingGetter
) {
  fun execute(scrapers: List<Scraper>): List<Result> {
    val executor = newFixedThreadPool(concurrency)
    return try {
      scrapers
        .flatMap { ScraperAdapter(getter, it).indexTasks }
        .map { executor.submit(it) }
        .flatMap { it.get() }
        .shuffled()   // Spread out requests to each brewery
        .map { executor.submit(it) }
        .mapNotNull { it.get() }
    } finally {
      executor.shutdownNow()
    }
  }

}
