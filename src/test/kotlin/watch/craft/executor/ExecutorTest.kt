package watch.craft.executor

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import watch.craft.Brewery
import watch.craft.Item
import watch.craft.Offer
import watch.craft.Scraper
import watch.craft.Scraper.Job
import watch.craft.Scraper.Job.Leaf
import watch.craft.Scraper.ScrapedItem
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
        override suspend fun retrieve(url: URI) = "<html><body><h1>Hello</h1></body></html>".toByteArray()
      }
    },
    clock = Clock.fixed(NOW, ZoneId.systemDefault())
  )

  @Test
  fun `scrapes products`() {
    val scraper = MyScraper(listOf(
      Leaf(name = "A", url = productUrl("a")) { product("Foo") },
      Leaf(name = "B", url = productUrl("b")) { product("Bar") }
    ))

    assertEquals(
      listOf(
        with(product("Bar")) {
          Item(
            brewery = BREWERY_NAME,
            name = name,
            summary = summary,
            desc = desc,
            mixed = mixed,
            abv = abv,
            offers = offers.toList(),
            available = available,
            new = true,
            thumbnailUrl = thumbnailUrl,
            url = productUrl("b")
          )
        },
        with(product("Foo")) {
          Item(
            brewery = BREWERY_NAME,
            name = name,
            summary = summary,
            desc = desc,
            mixed = mixed,
            abv = abv,
            offers = offers.toList(),
            available = available,
            new = true,
            thumbnailUrl = thumbnailUrl,
            url = productUrl("a")
          )
        }
      ),
      executor.scrape(listOf(scraper)).items
    )
  }

  @Test
  fun `continues after validation failure`() {
    val scraper = MyScraper(listOf(
      Leaf(name = "A", url = productUrl("a")) { product("Foo") },
      Leaf(name = "B", url = productUrl("b")) { product("Foo").copy(name = "") },  // Invalid name
      Leaf(name = "C", url = productUrl("c")) { product("Bar") }
    ))

    assertEquals(
      listOf("Bar", "Foo"),
      executor.scrape(listOf(scraper)).items.map { it.name }
    )
  }

  private class MyScraper(override val jobs: List<Job>) : Scraper {
    override val brewery = mock<Brewery> { on { shortName } doReturn BREWERY_NAME }
  }

  companion object {
    private const val BREWERY_NAME = "Foo Bar"
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
