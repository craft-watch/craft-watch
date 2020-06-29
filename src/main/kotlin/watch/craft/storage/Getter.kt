package watch.craft.storage

import java.net.URI

interface Getter<T> {
  fun request(url: URI): T
}
