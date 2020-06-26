package choliver.neapi.getters

import java.net.URI

class HttpGetter : Getter<String> {
  override fun request(url: URI) = url.toURL().readText()
}
