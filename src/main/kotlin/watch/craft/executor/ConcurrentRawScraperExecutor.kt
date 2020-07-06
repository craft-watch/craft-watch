package watch.craft.executor

import watch.craft.executor.ScraperAdapter.Result
import java.util.concurrent.Executors.newFixedThreadPool

class ConcurrentRawScraperExecutor(
  private val concurrency: Int = 4
) {
  fun execute(adapters: List<ScraperAdapter>): Set<Result> {
    val executor = newFixedThreadPool(concurrency)
    return try {
      adapters
        .flatMap { it.indexTasks }
        .map { executor.submit(it) }
        .flatMap { it.get() }
        .shuffled()   // Spread out requests to each brewery
        .map { executor.submit(it) }
        .mapNotNull { it.get() }
        .toSet()  // To make clear that order is not important
    } finally {
      executor.shutdownNow()
    }
  }
}
