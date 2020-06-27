package choliver.neapi.scrapers

import choliver.neapi.Scraper.Item
import choliver.neapi.executeScraper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.net.URI

class StewartScraperTest {
  companion object {
    private val ITEMS = executeScraper(StewartScraper())
  }

  @Test
  fun `finds all the beers`() {
    assertEquals(16, ITEMS.size)
  }

  @Test
  fun `extracts beer details`() {
    assertEquals(
      Item(
        name = "First World Problems",
        summary = "India Pale Ale",
        sizeMl = 330,
        abv = 6.2,
        perItemPrice = 1.80,
        available = true,
        thumbnailUrl = URI("https://www.stewartbrewing.co.uk/uploads/images/products/large/stewart-brewing-ltd-stewart-brewing-first-world-problems-1592394080330ml-can-FWP-002-.png")
      ),
      ITEMS.first { it.name == "First World Problems" }
    )
  }

  @Test
  fun `removes size from name`() {
    assertTrue(ITEMS.none { it.name.contains("ml") })
  }

  @Test
  fun `ignores things that aren't beers`() {
    assertEquals(
      emptyList<String>(),
      ITEMS.map { it.name }.filter { it.contains("glass", ignoreCase = true) }
    )
  }
}

