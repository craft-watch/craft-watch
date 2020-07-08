package watch.craft.network

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.options
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import watch.craft.FatalScraperException
import java.net.URI


class NetworkRetrieverTest {
  private val server = WireMockServer(options().dynamicPort())

  @BeforeEach
  fun beforeEach() {
    server.start()
  }

  @AfterEach
  fun afterEach() {
    server.stop()
  }

  @Test
  fun `retrieves a response from over the network`() {
    server.stubFor(get(urlEqualTo("/"))
      .willReturn(aResponse().withBody(NICE_DATA))
    )

    val data = NetworkRetriever("Yeah").use { retriever ->
      runBlocking {
        retriever.retrieve(URI("http://localhost:${server.port()}"))
      }
    }

    assertArrayEquals(NICE_DATA, data)
  }

  @Test
  fun `throws on network error`() {
    server.stubFor(get(urlEqualTo("/"))
      .willReturn(aResponse().withStatus(429))
    )

    assertThrows<FatalScraperException> {
      NetworkRetriever("Yeah").use { retriever ->
        runBlocking {
          retriever.retrieve(URI("http://localhost:${server.port()}"))
        }
      }
    }
  }

  companion object {
    private val NICE_DATA = byteArrayOf(1, 2, 3, 4, 5, 6, 7, 8)
  }
}
