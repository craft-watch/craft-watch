package watch.craft.executor

import com.nhaarman.mockitokotlin2.*
import kotlinx.coroutines.runBlocking
import org.jsoup.nodes.Document
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import watch.craft.*
import watch.craft.Scraper.Job
import watch.craft.Scraper.Job.Leaf
import watch.craft.Scraper.Job.More
import watch.craft.Scraper.ScrapedItem
import watch.craft.executor.ScraperAdapter.Result
import watch.craft.network.Retriever
import watch.craft.utils.textFrom
import java.net.URI

class ScraperAdapterTest {
  private val retriever = mock<Retriever> {
    onBlocking { retrieve(any(), any()) } doAnswer {
      "<html><body><h1>${it.getArgument<URI>(0)}</h1></body></html>".toByteArray()
    }
  }

  private val itemA = mock<ScrapedItem>()
  private val itemB = mock<ScrapedItem>()

  @Test
  fun `enriches item with results info`() {
    val adapter = ScraperAdapter(retriever, MyScraper(listOf(
      Leaf(name = "A", url = URL_A) { itemA }
    )))

    assertEquals(
      setOf(
        Result(breweryName = BREWERY_NAME, rawName = "A", url = URL_A, item = itemA)
      ),
      retrieveResults(adapter)
    )
  }

  @Test
  fun `passes correct URLs and HTML around`() {
    val work = mock<(Document) -> ScrapedItem> {
      on { invoke(any()) } doThrow SkipItemException("Emo town")
    }

    val adapter = ScraperAdapter(
      retriever, MyScraper(
      listOf(
        Leaf(name = "A", url = URL_A, work = work)
      )
    )
    )

    retrieveResults(adapter)

    verifyBlocking(retriever) { retrieve(URL_A, ".html") }
    verify(work)(docWithHeaderMatching(URL_A.toString()))
  }

  @Nested
  inner class Traversal {
    @Test
    fun `multiple flat items`() {
      val adapter = ScraperAdapter(retriever, MyScraper(listOf(
        Leaf(name = "A", url = URL_A) { itemA },
        Leaf(name = "A", url = URL_B) { itemB }
      )))

      assertEquals(setOf(itemA, itemB), retrieveItems(adapter))
    }

    @Test
    fun `non-leaf node`() {
      val adapter = ScraperAdapter(retriever, MyScraper(listOf(
        More(url = ROOT_URL) {
          listOf(
            Leaf(name = "A", url = URL_A) { itemA },
            Leaf(name = "A", url = URL_B) { itemB }
          )
        }
      )))

      assertEquals(setOf(itemA, itemB), retrieveItems(adapter))
    }

    @Test
    fun `multiple non-leaf nodes`() {
      val adapter = ScraperAdapter(retriever, MyScraper(listOf(
        More(url = ROOT_URL) {
          listOf(
            Leaf(name = "A", url = URL_A) { itemA },
            More(url = PAGE_2_URL) {
              listOf(
                Leaf(name = "A", url = URL_B) { itemB }
              )
            }
          )
        }
      )))

      assertEquals(setOf(itemA, itemB), retrieveItems(adapter))
    }
  }

  @Nested
  inner class ErrorHandling {
    @Test
    fun `fatal exception during request kills everything`() {
      retriever.stub {
        onBlocking { retriever.retrieve(any(), any()) } doThrow FatalScraperException("Uh oh")
      }

      val adapter = ScraperAdapter(retriever, MyScraper(listOf(
        More(url = ROOT_URL) {
          listOf(
            Leaf(name = "A", url = URL_A) { itemA },
            Leaf(name = "A", url = URL_B) { itemB }
          )
        }
      )))

      assertThrows<FatalScraperException> {
        retrieveResults(adapter)
      }
    }

    @Test
    fun `non-fatal exception during non-leaf scrape doesn't kill everything`() {
      val adapter = ScraperAdapter(retriever, MyScraper(listOf(
        More(url = ROOT_URL) {
          listOf(
            Leaf(name = "A", url = URL_A) { itemA },
            More(url = PAGE_2_URL) { throw MalformedInputException("Uh oh") }
          )
        }
      )))

      assertEquals(setOf(itemA), retrieveItems(adapter))    // Other item is returned
    }

    @Test
    fun `non-fatal exception during leaf scrape doesn't kill everything`() {
      val adapter = ScraperAdapter(retriever, MyScraper(listOf(
        More(url = ROOT_URL) {
          listOf(
            Leaf(name = "A", url = URL_A) { throw MalformedInputException("Uh oh") },
            Leaf(name = "A", url = URL_B) { itemB }
          )
        }
      )))

      assertEquals(setOf(itemB), retrieveItems(adapter))    // Other item is returned
    }

    @Test
    fun `skip exception during leaf scrape doesn't kill everything`() {
      val adapter = ScraperAdapter(retriever, MyScraper(listOf(
        More(url = ROOT_URL) {
          listOf(
            Leaf(name = "A", url = URL_A) { throw SkipItemException("Don't care") },
            Leaf(name = "A", url = URL_B) { itemB }
          )
        }
      )))

      assertEquals(setOf(itemB), retrieveItems(adapter))    // Other item is returned
    }
  }

  private fun retrieveItems(adapter: ScraperAdapter) = retrieveResults(adapter).map { it.item }.toSet()
  private fun retrieveResults(adapter: ScraperAdapter) = runBlocking { adapter.execute() }.results.toSet()

  private fun docWithHeaderMatching(header: String): Document = argForWhich { textFrom("h1") == header }

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
