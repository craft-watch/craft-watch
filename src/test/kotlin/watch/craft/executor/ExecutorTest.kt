package watch.craft.executor

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import watch.craft.Item
import watch.craft.Offer
import watch.craft.Scraper
import watch.craft.Scraper.Output
import watch.craft.Scraper.Output.Multiple
import watch.craft.Scraper.Output.ScrapedItem
import watch.craft.ScraperEntry
import watch.craft.dsl.work
import watch.craft.network.Retriever
import java.net.URI
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

class ExecutorTest {
  private val executor = Executor(
    results = mock(),
    createRetriever = {
      object : Retriever {
        override suspend fun retrieve(url: URI, suffix: String?, validate: (ByteArray) -> Unit) =
          "<html><body><h1>Hello</h1></body></html>".toByteArray()
      }
    },
    clock = Clock.fixed(NOW, ZoneId.systemDefault())
  )

  @Test
  fun `scrapes products`() {
    val scraper = scraper(listOf(
      work(name = "A", url = productUrl("a")) { product("Foo") },
      work(name = "B", url = productUrl("b")) { product("Bar") }
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
      work(name = "A", url = productUrl("a")) { product("Foo") },
      work(name = "B", url = productUrl("b")) { product("Foo").copy(name = "") },  // Invalid name
      work(name = "C", url = productUrl("c")) { product("Bar") }
    ))

    assertEquals(
      listOf("Bar", "Foo"),
      executor.scrape(scraper).items.map { it.name }
    )
  }

  private fun scraper(outputs: List<Output>) = listOf(
    ScraperEntry(
      scraper = object : Scraper {
        override val seed = Multiple(outputs)
      },
      brewery = mock { on { id } doReturn THIS_BREWERY_ID }
    )
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

    private fun productUrl(suffix: String) = URI("https://eaxmple.invalid/${suffix}")
  }
}
