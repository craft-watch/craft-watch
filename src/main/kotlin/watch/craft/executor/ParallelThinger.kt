package watch.craft.executor

import watch.craft.Scraper
import watch.craft.executor.ScraperAdapter.*
import watch.craft.storage.CachingGetter
import java.util.concurrent.Executors.newFixedThreadPool

class ParallelThinger(
  concurrency: Int = 4,
  private val getter: CachingGetter
) {
  private val executor = newFixedThreadPool(concurrency)

  fun execute(scrapers: List<Scraper>) = scrapers
    .flatMap { ScraperAdapter(getter, it).indexTasks }
    .executeParallel()
    .flatten()
    .executeParallel()
    .filterNotNull()

  private fun <R> List<() -> R>.executeParallel() = map { executor.submit(it) }.map { it.get() }
}
