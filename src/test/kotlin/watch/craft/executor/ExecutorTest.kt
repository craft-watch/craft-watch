package watch.craft.executor

import com.nhaarman.mockitokotlin2.mock
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import watch.craft.*
import watch.craft.Scraper.Node
import watch.craft.Scraper.Node.Retrieval
import watch.craft.Scraper.Node.ScrapedItem
import watch.craft.network.Retriever
import java.net.URI
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

class ExecutorTest {
  private val executor = Executor(
    results = mock(),
    createRetriever = { _, _ ->
      object : Retriever {
        override suspend fun retrieve(url: URI, suffix: String?, validate: (ByteArray) -> Unit) = byteArrayOf()
      }
    },
    clock = Clock.fixed(NOW, ZoneId.systemDefault())
  )

  @Test
  fun `scrapes products`() {
    val scraper = scraper(listOf(
      from(productUrl("a")) { product("Foo") },
      from(productUrl("b")) { product("Bar") }
    ))

    assertEquals(
      listOf(
        with(product("Bar")) {
          Item(
            breweryId = THIS_BREWERY_ID,
            name = name,
            summary = summary,
            desc = desc,
            mixed = mixed,
            abv = abv,
            offers = offers.toList(),
            available = available,
            new = false,
            thumbnailUrl = thumbnailUrl,
            url = productUrl("b")
          )
        },
        with(product("Foo")) {
          Item(
            breweryId = THIS_BREWERY_ID,
            name = name,
            summary = summary,
            desc = desc,
            mixed = mixed,
            abv = abv,
            offers = offers.toList(),
            available = available,
            new = false,
            thumbnailUrl = thumbnailUrl,
            url = productUrl("a")
          )
        }
      ),
      executor.scrape(scraper).items
    )
  }

  @Test
  fun `continues after validation failure`() {
    val scraper = scraper(listOf(
      from(productUrl("a")) { product("Foo") },
      from(productUrl("b")) { product("Foo").copy(name = "") },  // Invalid name
      from(productUrl("c")) { product("Bar") }
    ))

    assertEquals(
      listOf("Bar", "Foo"),
      executor.scrape(scraper).items.map { it.name }
    )
  }

  private fun scraper(nodes: List<Node>) = listOf(
    ScraperEntry(
      scraper = object : Scraper {
        override val roots = nodes
      },
      brewery = Brewery(
        id = THIS_BREWERY_ID,
        shortName = "",
        name = "",
        location = "",
        websiteUrl = URI("https://example.invalid")
      )
    )
  )

  private fun from(url: URI, block: (ByteArray) -> Node) = Retrieval(
    null,
    url,
    suffix = ".xxx",
    validate = { Unit },
    block = { data -> listOf(block(data())) }
  )

  companion object {
    private const val THIS_BREWERY_ID = "foo"
    private const val DECENT_PRICE = 2.46
    private val NOW = Instant.EPOCH

    private fun product(name: String) = ScrapedItem(
      name = name,
      summary = "${name} is great",
      offers = setOf(Offer(totalPrice = DECENT_PRICE, sizeMl = 440)),
      abv = 6.9,
      available = true,
      thumbnailUrl = URI("https://example.invalid/assets/${name}.jpg")
    )

    private fun productUrl(suffix: String) = URI("https://example.invalid/${suffix}")
  }
}
