package watch.craft.network

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.options
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doThrow
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import watch.craft.FatalScraperException
import watch.craft.MalformedInputException
import java.net.URI


class NetworkRetrieverTest {
  private val server = WireMockServer(options().dynamicPort())
  private val validate = mock<(ByteArray) -> Unit>()

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
    server.stubFor(
      get(urlEqualTo("/"))
        .willReturn(aResponse().withBody(NICE_DATA))
    )

    assertArrayEquals(NICE_DATA, retrieve())
  }

  // TODO - add coverage for timeouts, etc.

  @Test
  fun `fails immediately and throws on network error`() {
    server.stubFor(
      get(urlEqualTo("/"))
        .willReturn(aResponse().withStatus(429))
    )

    assertThrows<FatalScraperException> { retrieve() }
    assertEquals(1, server.allServeEvents.size)
  }

  @Test
  fun `retries and throws on validate error`() {
    whenever(validate(any())) doThrow MalformedInputException("Oh no")

    server.stubFor(
      get(urlEqualTo("/"))
        .willReturn(aResponse().withBody(NICE_DATA))
    )

    assertThrows<FatalScraperException> { retrieve() }
    assertEquals(5, server.allServeEvents.size)
  }

  private fun retrieve() = createRetriever().use { retriever ->
    runBlocking {
      retriever.retrieve(URI("http://localhost:${server.port()}"), validate = validate)
    }
  }

  private fun createRetriever() = NetworkRetriever("Yeah", rateLimitPeriodMillis = 50)

  companion object {
    private val NICE_DATA = byteArrayOf(1, 2, 3, 4, 5, 6, 7, 8)
  }
}
