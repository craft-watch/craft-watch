package choliver.neapi.getters

import choliver.neapi.storage.StorageThinger
import java.net.URI

class CachingGetter(
  private val storage: StorageThinger,
  private val delegate: Getter<String>
): Getter<String> {
  override fun request(url: URI) = storage.readFromHtmlCache(url.toString())
    ?: delegate.request(url).also { storage.writeToHtmlCache(url.toString(), it) }
}
