package watch.craft.dsl

import com.nhaarman.mockitokotlin2.*
import kotlinx.coroutines.runBlocking
import org.jsoup.nodes.Document
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import watch.craft.MalformedInputException
import watch.craft.Scraper.Node
import watch.craft.Scraper.Node.Retrieval
import java.net.URI


class NavigationTest {
  private data class Foo(
    val a: Int,
    val b: Int
  )

  @Nested
  inner class Json {
    private val goodJson = """{ "a": 123, "b": 456 }"""

    @Test
    fun lazy() {
      // No invocations
      val retrieval = createRetrieval { mock() }
      val source = mockSource(goodJson)

      execute(retrieval, source)

      verify(source, never())()
    }

    @Test
    fun memoizes() {
      // Invokes twice
      val retrieval = createRetrieval { foo -> foo(); foo(); mock() }
      val source = mockSource(goodJson)

      execute(retrieval, source)

      verify(source, times(1))()
    }

    @Test
    fun `parses object`() {
      val block = mock<(Foo) -> Node>()
      val retrieval = createRetrieval { foo -> block(foo()) }

      execute(retrieval, mockSource(goodJson))

      argumentCaptor<Foo>().apply {
        verify(block)(capture())
        assertEquals(Foo(123, 456), firstValue)
      }
    }

    private fun createRetrieval(block: suspend (Grab<Foo>) -> Node) =
      fromJson("foo", URI("https://example.invalid"), block)
  }

  @Nested
  inner class Html {
    private val goodHtml = "<html><head><title>Hello</title></head></html>"

    @Test
    fun lazy() {
      // No invocations
      val retrieval = createRetrieval { mock() }
      val source = mockSource(goodHtml)

      execute(retrieval, source)

      verify(source, never())()
    }

    @Test
    fun memoizes() {
      // Invokes twice
      val retrieval = createRetrieval { doc -> doc(); doc(); mock() }
      val source = mockSource(goodHtml)

      execute(retrieval, source)

      verify(source, times(1))()
    }

    @Test
    fun `parses document`() {
      val block = mock<(Document) -> Node>()
      val retrieval = createRetrieval { data -> block(data()) }

      execute(retrieval, mockSource(goodHtml))

      argumentCaptor<Document>().apply {
        verify(block)(capture())
        assertEquals("Hello", firstValue.title())
      }
    }

    @Test
    fun `throws on valid HTML without title`() {
      assertThrows<MalformedInputException> {
        createRetrieval { mock() }.validate("<html><head></head></html>".toByteArray())
      }
    }

    @Test
    fun `throws on invalid HTML`() {
      assertThrows<MalformedInputException> {
        createRetrieval { mock() }.validate("wat".toByteArray())
      }
    }

    private fun createRetrieval(block: suspend (Grab<Document>) -> Node) =
      fromHtml("foo", URI("https://example.invalid"), block)
  }

  private fun execute(retrieval: Retrieval, data: () -> String) {
    runBlocking {
      retrieval.block { data().toByteArray() }
    }
  }

  private fun mockSource(content: String) = mock<() -> String> { on { invoke() } doReturn content }
}
