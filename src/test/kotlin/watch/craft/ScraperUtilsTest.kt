package watch.craft

import org.jsoup.Jsoup
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ScraperUtilsTest {
  @Nested
  inner class NormaliseParagrahsFrom {

    @Test
    fun `drops headers`() {
      val doc = Jsoup.parse("""
        <html>
        <body>
        <div class="desc">
          <h1>Description</h1>
          <p>Hello there.</p>
          Wat.
          <h2>Some other stuff</h2>
          <p>Goodbye.</h2>
        </div>
        </body>
        </html>
      """.trimIndent())

      assertEquals(
        listOf(
          "Hello there.",
          "Wat.",
          "Goodbye.",
          ""    // Due to trailing newline
        ),
        doc.normaliseParagraphsFrom(".desc").split("\n")
      )
    }

    @Test
    fun `handles br`() {
      val doc = Jsoup.parse("""
        <html>
        <body>
        <div class="desc">
          <p>
            Hello mummy. <br/>
            What do you want?
          </p>
        </div>
        </body>
        </html>
      """.trimIndent())

      assertEquals(
        listOf(
          "Hello mummy.",
          "What do you want?",
          ""    // Due to trailing newline
        ),
        doc.normaliseParagraphsFrom(".desc").split("\n")
      )
    }

    /** Derived from something seen for real on Northern Monk product page. */
    @Test
    fun `deals with complex mess`() {
      val doc = Jsoup.parse("""
        <html>
        <body>
        <div class="desc">
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
        </div>
        </body>
        </html>
      """.trimIndent())

      assertEquals(
        listOf(
          "Foo Bar Baz",
          "Hello",
          "What.",
          "Naughty little men.",
          "Goodbye",
          ""    // Due to trailing newline
        ),
        doc.normaliseParagraphsFrom(".desc").split("\n")
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

}
