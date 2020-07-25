package watch.craft.dsl

import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.jsoup.nodes.Document
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import watch.craft.MalformedInputException
import watch.craft.Scraper.Node
import java.net.URI

class NavigationTest {
  @Nested
  inner class Html {
    private val retrieval = fromHtml(
      name = "foo",
      url = URI("https://example.invalid"),
      block = { mock() }
    )

    @Test
    fun `parses document`() {
      val block = mock<(Document) -> Node>()
      val retrieval = fromHtml(
        name = "foo",
        url = URI("https://example.invalid"),
        block = block
      )

      retrieval.block("<html><head><title>Hello</title></head></html>".toByteArray())

      argumentCaptor<Document>().apply {
        verify(block)(capture())
        assertEquals("Hello", firstValue.title())
      }
    }

    @Test
    fun `doesn't throw on valid HTML with title`() {
      assertDoesNotThrow {
        retrieval.validate("<html><head><title>Hello</title></head></html>".toByteArray())
      }
    }

    @Test
    fun `throws on valid HTML without title`() {
      assertThrows<MalformedInputException> {
        retrieval.validate("<html><head></head></html>".toByteArray())
      }
    }

    @Test
    fun `throws on invalid HTML`() {
      assertThrows<MalformedInputException> {
        retrieval.validate("wat".toByteArray())
      }
    }
  }
}
