package watch.craft.getters

import java.net.URI

interface Getter<T> {
  fun request(url: URI): T
}
