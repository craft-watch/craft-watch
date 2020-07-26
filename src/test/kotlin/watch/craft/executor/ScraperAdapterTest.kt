package watch.craft.executor

import com.nhaarman.mockitokotlin2.*
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import watch.craft.*
import watch.craft.Scraper.Node
import watch.craft.Scraper.Node.Retrieval
import watch.craft.Scraper.Node.ScrapedItem
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
      val block = mock<(ByteArray) -> List<Node>> {
        on { invoke(any()) } doReturn listOf(itemA)
      }

      execute(
        adapter(
          Retrieval(
            null,
            URL_A,
            suffix = COOL_SUFFIX,
            validate = validate,
            block = { data -> block(data()) }
          )
        )
      )

      verifyBlocking(retriever) { retrieve(URL_A, COOL_SUFFIX, validate) }
      verify(block)(URL_A.toString().toByteArray())
    }

    @Test
    fun lazy() {
      execute(
        adapter(
          Retrieval(
            null,
            URL_A,
            suffix = COOL_SUFFIX,
            validate = { },
            block = { listOf(itemA) }  // No data invocations
          )
        )
      )

      verifyBlocking(retriever, never()) { retrieve(any(), any(), any()) }
    }
  }

  @Nested
  inner class Traversal {
    @Test
    fun `retrieval producing single item`() {
      val adapter = adapter(
        from(URL_A) { listOf(itemA) }
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
        from(URL_A) { listOf(itemA) },
        from(URL_B) { listOf(itemB) }
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
        from(URL_A) { listOf(itemA, itemB) }
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
          listOf(
            from(URL_A) { listOf(itemA) },
            from(PAGE_2_URL) {
              listOf(from(URL_B) { listOf(itemB) })
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
          listOf(
            from(URL_A) { throw FatalScraperException("Uh oh") },
            from(URL_B) { listOf(itemB) }
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
          listOf(
            from(URL_A) { throw UnretrievableException("Uh oh") },
            from(URL_B) { listOf(itemB) }
          )
        }
      )

      assertEquals(listOf(itemB), execute(adapter).items())   // Other item is returned
    }

    @Test
    fun `skip exception during work scrape doesn't kill everything`() {
      val adapter = adapter(
        from(ROOT_URL) {
          listOf(
            from(URL_A) { throw SkipItemException("Don't care") },
            from(URL_B) { listOf(itemB) }
          )
        }
      )

      assertEquals(listOf(itemB), execute(adapter).items())    // Other item is returned
    }

    @Test
    fun `going very deep is terminated, but doesn't kill everything`() {
      var deepTree: Node = itemB
      deepTree = from(URL_B) { listOf(deepTree) }

      val adapter = adapter(
        from(ROOT_URL) {
          listOf(
            from(URL_A) { listOf(itemA) },
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
      listOf(
        from(URL_A) { data -> listOf(block(data)) }
      )
    }
  )

  private fun adapter(vararg roots: Node) = ScraperAdapter(
    retriever,
    object : Scraper {
      override val roots = roots.toList()
    },
    BREWERY_ID
  )

  private fun from(url: URI, block: (ByteArray) -> List<Node>) = Retrieval(
    null,
    url,
    suffix = COOL_SUFFIX,
    validate = { Unit },
    block = { data -> block(data()) }
  )

  companion object {
    private const val BREWERY_ID = "foo"
    private val ROOT_URL = URI("https://example.invalid")
    private val PAGE_2_URL = URI("https://example.invalid/2")
    private val URL_A = URI("https://example.invalid/a")
    private val URL_B = URI("https://example.invalid/b")
    private const val COOL_SUFFIX = ".xxx"
  }
}
