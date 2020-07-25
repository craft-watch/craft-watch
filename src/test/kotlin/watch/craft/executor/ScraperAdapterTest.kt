package watch.craft.executor

import com.nhaarman.mockitokotlin2.*
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import watch.craft.*
import watch.craft.Scraper.Node
import watch.craft.Scraper.Node.*
import watch.craft.executor.ScraperAdapter.Result
import watch.craft.network.Retriever
import java.net.URI

class ScraperAdapterTest {
  private val retriever = mock<Retriever> {
    onBlocking { retrieve(any(), any(), any()) } doAnswer { it.getArgument<URI>(0).toString().toByteArray() }
  }

  private val itemA = mock<ScrapedItem>()
  private val itemB = mock<ScrapedItem>()

  @Nested
  inner class Plumbing {
    @Test
    fun `passes correct URLs and data around`() {
      val validate = mock<(ByteArray) -> Unit>()
      val block = mock<(ByteArray) -> ScrapedItem> {
        on { invoke(any()) } doReturn itemA
      }

      execute(
        adapter(
          Retrieval(
            null,
            URL_A,
            suffix = COOL_SUFFIX,
            validate = validate,
            block = block
          )
        )
      )

      verifyBlocking(retriever) { retrieve(URL_A, COOL_SUFFIX, validate) }
      verify(block)(URL_A.toString().toByteArray())
    }
  }

  @Nested
  inner class Traversal {
    @Test
    fun `retrieval producing single item`() {
      val adapter = adapter(
        from(URL_A) { itemA }
      )

      assertEquals(
        listOf(
          Result(BREWERY_ID, URL_A, itemA)
        ),
        execute(adapter).entries
      )
    }

    @Test
    fun `multiple retrievals producing single items`() {
      val adapter = adapter(
        multiple(
          from(URL_A) { itemA },
          from(URL_B) { itemB }
        )
      )

      assertEquals(
        listOf(
          Result(BREWERY_ID, URL_A, itemA),
          Result(BREWERY_ID, URL_B, itemB)
        ),
        execute(adapter).entries
      )
    }

    @Test
    fun `retrieval producing multiple items`() {
      val adapter = adapter(
        from(URL_A) { multiple(itemA, itemB) }
      )

      assertEquals(
        listOf(
          Result(BREWERY_ID, URL_A, itemA),
          Result(BREWERY_ID, URL_A, itemB)      // Both associated with same source URL
        ),
        execute(adapter).entries
      )
    }

    @Test
    fun `more depth`() {
      val adapter = adapter(
        from(ROOT_URL) {
          multiple(
            from(URL_A) { itemA },
            from(PAGE_2_URL) {
              from(URL_B) { itemB }
            }
          )
        }
      )

      assertEquals(
        listOf(
          Result(BREWERY_ID, URL_A, itemA),
          Result(BREWERY_ID, URL_B, itemB)
        ),
        execute(adapter).entries
      )
    }
  }

  @Nested
  inner class ErrorHandling {
    @Test
    fun `fatal exception kills everything`() {
      val adapter = adapter(
        from(ROOT_URL) {
          multiple(
            from(URL_A) { throw FatalScraperException("Uh oh") },
            from(URL_B) { itemB }
          )
        }
      )

      assertThrows<FatalScraperException> {
        execute(adapter)
      }
    }

    @Test
    fun `non-fatal exception doesn't kill everything`() {
      val adapter = adapter(
        from(ROOT_URL) {
          multiple(
            from(URL_A) { throw UnretrievableException("Uh oh") },
            from(URL_B) { itemB }
          )
        }
      )

      assertEquals(listOf(itemB), execute(adapter).items())   // Other item is returned
    }

    @Test
    fun `skip exception during work scrape doesn't kill everything`() {
      val adapter = adapter(
        from(ROOT_URL) {
          multiple(
            from(URL_A) { throw SkipItemException("Don't care") },
            from(URL_B) { itemB }
          )
        }
      )

      assertEquals(listOf(itemB), execute(adapter).items())    // Other item is returned
    }

    @Test
    fun `going very deep is terminated, but doesn't kill everything`() {
      var deepTree: Node = itemB
      deepTree = from(URL_B) { deepTree }

      val adapter = adapter(
        from(ROOT_URL) {
          multiple(
            from(URL_A) { itemA },
            deepTree
          )
        }
      )

      assertEquals(listOf(itemA), execute(adapter).items())    // Other item is returned
    }
  }

  @Nested
  inner class Stats {
    @Test
    fun `counts normal`() {
      val adapter = adapterWithSingleLeaf { itemA }

      assertEquals(1, execute(adapter).stats.numScraped)
    }

    @Test
    fun `counts malformed`() {
      val adapter = adapterWithSingleLeaf { throw MalformedInputException("Don't care") }

      assertEquals(1, execute(adapter).stats.numMalformed)
    }

    @Test
    fun `counts unretrievable`() {
      val adapter = adapterWithSingleLeaf { throw UnretrievableException("Don't care") }

      assertEquals(1, execute(adapter).stats.numUnretrievable)
    }

    @Test
    fun `counts errors`() {
      val adapter = adapterWithSingleLeaf { throw RuntimeException("Don't care") }

      assertEquals(1, execute(adapter).stats.numErrors)
    }

    @Test
    fun `counts max-depth-exceeded as error`() {
      val adapter = adapterWithSingleLeaf { throw MaxDepthExceededException("Don't care") }

      assertEquals(1, execute(adapter).stats.numErrors)
    }

    @Test
    fun `counts skipped`() {
      val adapter = adapterWithSingleLeaf { throw SkipItemException("Don't care") }

      assertEquals(1, execute(adapter).stats.numSkipped)
    }
  }

  private fun StatsWith<Result>.items() = entries.map { it.item }

  private fun execute(adapter: ScraperAdapter) = runBlocking { adapter.execute() }

  private fun adapterWithSingleLeaf(block: (ByteArray) -> ScrapedItem) = adapter(
    from(ROOT_URL) {
      multiple(
        from(URL_A, block)
      )
    }
  )

  private fun adapter(node: Node) = ScraperAdapter(
    retriever,
    object : Scraper {
      override val root = node
    },
    BREWERY_ID
  )

  private fun from(url: URI, block: (ByteArray) -> Node) = Retrieval(
    null,
    url,
    suffix = COOL_SUFFIX,
    validate = { Unit },
    block = block
  )

  private fun multiple(vararg nodes: Node) = Multiple(nodes.toList())

  companion object {
    private const val BREWERY_ID = "foo"
    private val ROOT_URL = URI("https://example.invalid")
    private val PAGE_2_URL = URI("https://example.invalid/2")
    private val URL_A = URI("https://example.invalid/a")
    private val URL_B = URI("https://example.invalid/b")
    private const val COOL_SUFFIX = ".xxx"
  }
}
