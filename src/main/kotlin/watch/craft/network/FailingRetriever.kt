package watch.craft.network

import watch.craft.FatalScraperException
import java.net.URI

class FailingRetriever : Retriever {
  override suspend fun retrieve(url: URI): ByteArray {
    throw FatalScraperException("Non-live tests should not perform network gets")
  }
}
