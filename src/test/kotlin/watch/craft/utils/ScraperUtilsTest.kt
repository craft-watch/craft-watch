package watch.craft.utils

import org.jsoup.Jsoup
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import watch.craft.MalformedInputException

class ScraperUtilsTest {
  @Nested
  inner class SizeFrom {
    @Test
    fun millilitres() {
      assertEquals(550, "550 ml".sizeMlFrom())
      assertEquals(550, "550 mL".sizeMlFrom())
      assertEquals(550, "550 ML".sizeMlFrom())
      assertEquals(550, "550   ml".sizeMlFrom())
      assertEquals(550, "550ml".sizeMlFrom())
    }

    @Test
    fun litres() {
      assertEquals(3000, "3 litre".sizeMlFrom())
      assertEquals(3000, "3 litres".sizeMlFrom())
      assertEquals(3000, "3L".sizeMlFrom())
      assertEquals(3000, "3l".sizeMlFrom())
      assertEquals(3000, "3-litre".sizeMlFrom())
    }

    @Test
    fun `true negatives`() {
      assertThrows<MalformedInputException> { "3 llamas".sizeMlFrom() }
      assertThrows<MalformedInputException> { "550 mlady".sizeMlFrom() }
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

  @Nested
  inner class ToTitleCase {
    @Test
    fun `handles degenerate cases`() {
      assertEquals("", "".toTitleCase())
      assertEquals(" ", " ".toTitleCase())
      assertEquals("  ", "  ".toTitleCase())
      assertEquals("  Foo  ", "  Foo  ".toTitleCase())
    }


    @Test
    fun `converts simple examples`() {
      assertEquals("Foo Bar Baz", "Foo Bar Baz".toTitleCase())
      assertEquals("Foo Bar Baz", "foo bar baz".toTitleCase())
      assertEquals("Foo Bar Baz", "fOO bAR bAZ".toTitleCase())
      assertEquals("Foo Bar Baz", "FOO BAR BAZ".toTitleCase())
    }

    @Test
    fun `doesn't convert beer acronyms`() {
      assertEquals("Foo Bar IPA", "Foo Bar IPA".toTitleCase())
      assertEquals("Foo Baripa", "Foo BARIPA".toTitleCase())  // Not treated as a beer acronym
    }

    @Test
    fun `handles multiple whitespaces`() {
      assertEquals("Foo  Bar  Baz", "FOO  BAR  BAZ".toTitleCase())
    }

    @Test
    fun `treats punctuation as separator`() {
      assertEquals("Foo-Bar-Baz", "FOO-BAR-BAZ".toTitleCase())
      assertEquals("Foo (Bar Baz)", "FOO (BAR BAZ)".toTitleCase())
    }

    @Test
    fun `doesn't treat apostrophes as separator`() {
      assertEquals("Foo's Beer", "FOO'S BEER".toTitleCase())
    }

    @Test
    fun `doesn't treat non-ASCII alphanumeric as separator`() {
      assertEquals("Béér", "BÉÉR".toTitleCase())
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
