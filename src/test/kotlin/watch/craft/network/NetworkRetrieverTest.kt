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
import watch.craft.MalformedInputException
import watch.craft.UnretrievableException
import watch.craft.network.NetworkRetriever.Config
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

  @Test
  fun `follows redirects`() {
    server.stubFor(
      get(urlEqualTo("/"))
        .willReturn(temporaryRedirect("/alternate"))
    )
    server.stubFor(
      get(urlEqualTo("/alternate"))
        .willReturn(aResponse().withBody(NICE_DATA))
    )

    assertArrayEquals(NICE_DATA, retrieve())
  }

  // TODO - add coverage for timeouts, etc.

  @Test
  fun `fails immediately and throws on network error`() {
    server.stubFor(
      get(urlEqualTo("/"))
        .willReturn(aResponse().withStatus(404))
    )

    assertThrows<UnretrievableException> { retrieve() }
    assertEquals(1, server.allServeEvents.size)
  }

  @Test
  fun `returns response body on 404 if configured to ignore 404s`() {
    server.stubFor(
      get(urlEqualTo("/"))
        .willReturn(aResponse().withStatus(404).withBody(NICE_DATA))
    )

    assertArrayEquals(NICE_DATA, retrieve(STANDARD_CONFIG.copy(failOn404 = false)))
  }

  @Test
  fun `fails immediately on non-404 if configured to ignore 404s`() {
    server.stubFor(
      get(urlEqualTo("/"))
        .willReturn(aResponse().withStatus(429).withBody(NICE_DATA))
    )

    assertThrows<UnretrievableException> { retrieve() }
    assertEquals(1, server.allServeEvents.size)
  }

  @Test
  fun `retries and throws on validate error`() {
    whenever(validate(any())) doThrow MalformedInputException("Oh no")

    server.stubFor(
      get(urlEqualTo("/"))
        .willReturn(aResponse().withBody(NICE_DATA))
    )

    assertThrows<UnretrievableException> { retrieve() }
    assertEquals(5, server.allServeEvents.size)
  }

  private fun retrieve(config: Config = STANDARD_CONFIG) =
    NetworkRetriever(config).use { retriever ->
      runBlocking {
        retriever.retrieve(URI("http://localhost:${server.port()}"), validate = validate)
      }
    }

  companion object {
    private val NICE_DATA = byteArrayOf(1, 2, 3, 4, 5, 6, 7, 8)

    private val STANDARD_CONFIG = Config(id = "Yeah", rateLimitPeriodMillis = 50)
  }
}
