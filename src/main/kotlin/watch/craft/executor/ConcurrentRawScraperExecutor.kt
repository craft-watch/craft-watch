package watch.craft.executor

import kotlinx.coroutines.*
import watch.craft.executor.ScraperAdapter.Result

class ConcurrentRawScraperExecutor(
  private val rateLimitPeriodMillis: Int = 3000
) {
  fun execute(adapters: List<ScraperAdapter>): Set<Result> {
    return runBlocking {
      adapters
        .flatMap { it.indexTasks }
        .map { rootTask ->
          async {
            onIoThread(rootTask)
              .mapIndexed { idx, itemTask ->
                async {
                  delay(idx * rateLimitPeriodMillis.toLong())
                  onIoThread(itemTask)
                }
              }
              .mapNotNull { it.await() }
          }
        }
        .flatMap { it.await() }
        .toSet()  // To make clear that order is not important
    }
  }

  private suspend fun <R> onIoThread(task: () -> R) = withContext(Dispatchers.IO) { task() }
}
