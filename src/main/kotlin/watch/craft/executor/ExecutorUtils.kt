package watch.craft.executor

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

suspend fun <R> onIoThread(block: () -> R) = withContext(Dispatchers.IO) { block() }
