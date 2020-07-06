package watch.craft.executor

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import watch.craft.Brewery
import watch.craft.Scraper
import watch.craft.Scraper.Job
import watch.craft.Scraper.Job.Leaf
import watch.craft.Scraper.Job.More
import watch.craft.Scraper.ScrapedItem
import watch.craft.executor.ScraperAdapter.Result
import watch.craft.storage.CachingGetter
import java.net.URI

class ScraperAdapterTest {
  // TODO - test that doc gets passed in
  // TODO - error-handling
  // TODO - test structured concurrency on error

  private val getter = mock<CachingGetter> {
    on { request(any()) } doReturn "<html><body><h1>Hello</h1></body></html>".toByteArray()
  }

  private val itemA = mock<ScrapedItem>()
  private val itemB = mock<ScrapedItem>()

  @Test
  fun `enriches item with results info`() {
    val adapter = ScraperAdapter(getter, MyScraper(listOf(
      Leaf(rawName = "A", url = URL_A) { itemA }
    )))

    assertEquals(
      setOf(
        Result(breweryName = BREWERY_NAME, rawName = "A", url = URL_A, item = itemA)
      ),
      retrieveResults(adapter)
    )
  }

  @Test
  fun `executes and collates multiple items`() {
    val adapter = ScraperAdapter(getter, MyScraper(listOf(
      Leaf(rawName = "A", url = URL_A) { itemA },
      Leaf(rawName = "A", url = URL_B) { itemB }
    )))

    assertEquals(
      setOf(itemA, itemB),
      retrieveItems(adapter)
    )
  }

  @Test
  fun `traverses non-leaf node to obtain items`() {
    val adapter = ScraperAdapter(getter, MyScraper(listOf(
      More(url = ROOT_URL) {
        listOf(
          Leaf(rawName = "A", url = URL_A) { itemA },
          Leaf(rawName = "A", url = URL_B) { itemB }
        )
      }
    )))

    assertEquals(
      setOf(itemA, itemB),
      retrieveItems(adapter)
    )
  }

  @Test
  fun `traverses multiple non-leaf nodes to obtain all items`() {
    val adapter = ScraperAdapter(getter, MyScraper(listOf(
      More(url = ROOT_URL) {
        listOf(
          Leaf(rawName = "A", url = URL_A) { itemA },
          More(url = PAGE_2_URL) {
            listOf(
              Leaf(rawName = "A", url = URL_B) { itemB }
            )
          }
        )
      }
    )))

    assertEquals(
      setOf(itemA, itemB),
      retrieveItems(adapter)
    )
  }

  private fun retrieveResults(adapter: ScraperAdapter) = runBlocking { adapter.execute() }.toSet()
  private fun retrieveItems(adapter: ScraperAdapter) = runBlocking { adapter.execute() }.map { it.item }.toSet()

  private class MyScraper(override val jobs: List<Job>) : Scraper {
    override val brewery = mock<Brewery> { on { shortName } doReturn BREWERY_NAME }
  }

  companion object {
    private const val BREWERY_NAME = "Foo"
    private val ROOT_URL = URI("https://example.invalid")
    private val PAGE_2_URL = URI("https://example.invalid/2")
    private val URL_A = URI("https://example.invalid/a")
    private val URL_B = URI("https://example.invalid/a")
  }
}
