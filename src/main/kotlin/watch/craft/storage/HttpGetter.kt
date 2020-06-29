package watch.craft.storage

import mu.KotlinLogging
import java.net.URI

class HttpGetter : Getter {
  private val logger = KotlinLogging.logger {}

  override fun request(url: URI): ByteArray {
    logger.info("${url}: reading from network")
    return url.toURL().readBytes()
  }
}
