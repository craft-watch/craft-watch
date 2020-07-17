package watch.craft.executor

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import watch.craft.BreweryStats
import watch.craft.Offer
import watch.craft.Scraper.ScrapedItem
import watch.craft.executor.ScraperAdapter.Result
import java.net.URI

class ItemNormalisationTest {
  @Test
  fun `trims name`() {
    assertEquals(
      "Padded Lager",
      normalise(prototype.copy(name = "  Padded Lager  ")).entries.single().name
    )
  }

  @Test
  fun `trims summary`() {
    assertEquals(
      "Absolute nonsense",
      normalise(prototype.copy(summary = "  Absolute nonsense  ")).entries.single().summary
    )
  }

  @Test
  fun `trims desc`() {
    assertEquals(
      "Lorem ipsum",
      normalise(prototype.copy(desc = "  Lorem ipsum  ")).entries.single().desc
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

  @Test
  fun `rejects if no offers`() {
    assertNoValidationFailure(prototype)
    assertValidationFailure(prototype.copy(offers = emptySet()))
  }

  private fun assertNoValidationFailure(item: ScrapedItem) {
    val ret = normalise(item)
    assertTrue(ret.entries.isNotEmpty())
    assertEquals(0, ret.stats.numInvalid)
  }

  private fun assertValidationFailure(item: ScrapedItem) {
    val ret = normalise(item)
    assertTrue(ret.entries.isEmpty())
    assertEquals(1, ret.stats.numInvalid)
  }

  private fun normalise(
    item: ScrapedItem = prototype,
    url: URI = URI("https://example.invalid/shop")
  ) = StatsWith(
    entries = listOf(
      Result(
        breweryId = "foo",
        rawName = "",
        url = url,
        item = item
      )
    ),
    stats = BreweryStats("foo")
  ).normaliseToItems()

  private val prototype = ScrapedItem(
    name = "Ted Shandy",
    summary = "Awful",
    offers = setOf(Offer(quantity = 2, totalPrice = 3.72, sizeMl = 330)),
    abv = 1.2,
    available = true,
    thumbnailUrl = URI("https://example.invalid/assets/ted-shandy.jpg")
  )
}
