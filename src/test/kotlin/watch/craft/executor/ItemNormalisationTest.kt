package watch.craft.executor

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import watch.craft.InvalidItemException
import watch.craft.Offer
import watch.craft.Scraper.ScrapedItem
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
  fun `removes noise from thumbnail URLs`() {
    assertEquals(
      URI("https://example.invalid/s/files/1/2/3/4/my_img.jpg"),
      normalise(prototype.copy(thumbnailUrl = URI("https://example.invalid/s/files/1/2/3/4/my_img.jpg?v=1593009635"))).thumbnailUrl
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
    assertNoValidationFailure(
      prototype.copy(
        offers = setOf(
          Offer(
            quantity = 2,
            totalPrice = 14.0
          )
        )
      )
    ) // Apparently this is a reasonable price
    assertValidationFailure(prototype.copy(offers = setOf(Offer(quantity = 2, totalPrice = 20.0)))) // But this is mad
  }

  private fun assertNoValidationFailure(item: ScrapedItem) {
    assertDoesNotThrow {
      normalise(item)
    }
  }

  private fun assertValidationFailure(item: ScrapedItem) {
    assertThrows<InvalidItemException> {
      normalise(item)
    }
  }

  private fun normalise(
    item: ScrapedItem = prototype,
    brewery: String = "Foo Bar",
    url: URI = URI("https://example.invalid/shop")
  ) = ScraperAdapter.Result(
    breweryName = brewery,
    rawName = "",
    url = url,
    item = item
  ).normalise()

  private val prototype = ScrapedItem(
    name = "Ted Shandy",
    summary = "Awful",
    offers = setOf(Offer(quantity = 2, totalPrice = 3.72, sizeMl = 330)),
    abv = 1.2,
    available = true,
    thumbnailUrl = URI("https://example.invalid/assets/ted-shandy.jpg")
  )
}
