package watch.craft

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ScraperUtilsTest {

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
