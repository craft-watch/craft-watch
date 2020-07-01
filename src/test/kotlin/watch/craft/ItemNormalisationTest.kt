package watch.craft

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import java.net.URI

class ItemNormalisationTest {
  @Test
  fun `trims name`() {
    assertEquals(
      "Padded Lager",
      normalise(prototype.copy(name = "  Padded Lager  ")).name
    )
  }

  @Test
  fun `trims summary`() {
    assertEquals(
      "Absolute nonsense",
      normalise(prototype.copy(summary = "  Absolute nonsense  ")).summary
    )
  }

  @Test
  fun `trims desc`() {
    assertEquals(
      "Lorem ipsum",
      normalise(prototype.copy(desc = "  Lorem ipsum  ")).desc
    )
  }

  @Test
  fun `rejects if name is blank`() {
    assertNoValidationFailure(prototype.copy(name = "Yeah"))
    assertValidationFailure(prototype.copy(name = " "))
  }

  @Test
  fun `rejects if summary is present and blank`() {
    assertNoValidationFailure(prototype.copy(summary = "Yeah"))
    assertNoValidationFailure(prototype.copy(summary = null))
    assertValidationFailure(prototype.copy(summary = " "))
  }

  @Test
  fun `rejects if description is present and blank`() {
    assertNoValidationFailure(prototype.copy(desc = "Yeah"))
    assertNoValidationFailure(prototype.copy(desc = null))
    assertValidationFailure(prototype.copy(desc = " "))
  }

  @Test
  fun `rejects if gross`() {
    assertNoValidationFailure(prototype.copy(abv = 12.0))
    assertValidationFailure(prototype.copy(abv = 15.0))
  }

  @Test
  fun `rejects if too bougie`() {
    assertNoValidationFailure(prototype.copy(price = 14.0))
    assertValidationFailure(prototype.copy(price = 20.0))
  }

  private fun assertNoValidationFailure(item: Scraper.Item) {
    assertDoesNotThrow {
      normalise(item)
    }
  }

  private fun assertValidationFailure(item: Scraper.Item) {
    assertThrows<InvalidItemException> {
      normalise(item)
    }
  }

  private fun normalise(
    item: Scraper.Item = prototype,
    brewery: String = "Foo Bar",
    url: URI = URI("https://example.invalid/shop")
  ) = item.normalise(brewery, url)

  private val prototype = Scraper.Item(
    name = "Ted Shandy",
    summary = "Awful",
    numItems = 2,
    price = 3.72,
    sizeMl = 330,
    abv = 1.2,
    available = true,
    thumbnailUrl = URI("https://example.invalid/assets/ted-shandy.jpg")
  )
}
