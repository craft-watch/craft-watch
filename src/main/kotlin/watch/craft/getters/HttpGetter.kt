package watch.craft.getters

import mu.KotlinLogging
import java.net.URI

class HttpGetter : Getter<String> {
  private val logger = KotlinLogging.logger {}

  override fun request(url: URI): String {
    logger.info("${url}: reading from network")
    return url.toURL().readText()
  }
}
