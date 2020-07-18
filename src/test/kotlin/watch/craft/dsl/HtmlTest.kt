package watch.craft.dsl

import org.jsoup.Jsoup
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.net.URI

class HtmlTest {
  @Nested
  inner class UrlFrom {
    @Test
    fun `removes noise from thumbnail URLs`() {
      val doc = docFromFragment(
        """<img src="https://example.invalid/s/files/1/2/3/4/my_img.jpg?v=1593009635" />"""
      )

      assertEquals(
        URI("https://example.invalid/s/files/1/2/3/4/my_img.jpg"),
        doc.urlFrom("img")
      )
    }
  }

  @Nested
  inner class FormattedTextFrom {
    @Test
    fun `drops headers`() {
      val doc = docFromFragment(
        """
        <h1>Description</h1>
        <p>Hello there.</p>
        Wat.
        <h2>Some other stuff</h2>
        <p>Goodbye.</h2>
      """
      )

      assertEquals(
        listOf(
          "Hello there.",
          "Wat.",
          "Goodbye."
        ),
        doc.formattedTextFrom(TARGET).split("\n")
      )
    }

    @Test
    fun `handles br`() {
      val doc = docFromFragment(
        """
        <p>
          Hello mummy. <br/>
          What do you want?
        </p>
      """
      )

      assertEquals(
        listOf(
          "Hello mummy.",
          "What do you want?"
        ),
        doc.formattedTextFrom(TARGET).split("\n")
      )
    }

    @Test
    fun `retains text from inline elements`() {
      val doc = docFromFragment(
        """
        <p>
          Hello mummy. <a>What would you like</a> for tea?
        </p>
      """
      )

      assertEquals(
        listOf(
          "Hello mummy. What would you like for tea?"
        ),
        doc.formattedTextFrom(TARGET).split("\n")
      )
    }

    /** Derived from something seen for real on Northern Monk product page. */
    @Test
    fun `deals with complex mess`() {
      val doc = docFromFragment(
        """
        <p>
          <span>
            <strong>
              Foo
              <span>Bar</span>
              <span>Baz</span>
            </strong>
          </span>
        </p>

        <p>
          <strong>Hello</strong>
        </p>

        <div>
          <div>
            <div>
              <div>

                <p><span>What. </span></p>

                <p><span>Naughty </span>little men.</p>

                <p><span>Goodbye </span></p>
              </div>
            </div>
          </div>
        </div>
      """
      )

      assertEquals(
        listOf(
          "Foo Bar Baz",
          "Hello",
          "What.",
          "Naughty little men.",
          "Goodbye"
        ),
        doc.formattedTextFrom(TARGET).split("\n")
      )
    }

    @Test
    fun `retains all text in a top-level non-div`() {
      val doc = docFromFragment("Hello there", topLevelTag = "a")

      assertEquals(
        listOf("Hello there"),
        doc.formattedTextFrom(TARGET).split("\n")
      )
    }
  }

  private fun docFromFragment(raw: String, topLevelTag: String = "div") = Jsoup.parse(
    """
    <html>
    <body>
    <${topLevelTag} class="${TARGET.removePrefix(".")}">${raw}</${topLevelTag}>
    </body>
    </html>
  """.trimIndent()
  )

  companion object {
    private const val TARGET = ".target"
  }
}
