package choliver.neapi.getters

import java.net.URI

interface Getter<T> {
  fun request(url: URI): T
}
