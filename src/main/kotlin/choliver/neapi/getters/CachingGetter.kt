package choliver.neapi.getters

import choliver.neapi.storage.HtmlCache
import java.net.URI

class CachingGetter(
  private val cache: HtmlCache,
  private val delegate: Getter<String>
): Getter<String> {
  override fun request(url: URI) = cache.read(url.toString())
    ?: delegate.request(url).also { cache.write(url.toString(), it) }
}
