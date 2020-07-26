package watch.craft.utils

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock


fun <T : Any> memoize(f: suspend () -> T): suspend () -> T {
  val mutex = Mutex()
  var data: T? = null
  return {
    mutex.withLock {
      data = data ?: f()
      data!!
    }
  }
}
