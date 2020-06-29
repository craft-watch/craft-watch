package watch.craft.storage

import java.net.URI

interface Getter {
  fun request(url: URI): ByteArray
}
