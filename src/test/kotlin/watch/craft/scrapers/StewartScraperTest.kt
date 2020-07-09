package watch.craft.scrapers

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import watch.craft.Scraper.ScrapedItem
import watch.craft.byName
import watch.craft.executeScraper
import java.net.URI

class StewartScraperTest {
  companion object {
    private val ITEMS = executeScraper(StewartScraper())
  }

  @Test
  fun `finds all the beers`() {
    assertEquals(12, ITEMS.size)
  }

  @Test
  fun `extracts beer details`() {
    assertEquals(
      ScrapedItem(
        name = "Cascadian East",
        summary = "India Pale Ale",
        sizeMl = 330,
        abv = 5.4,
        totalPrice = 2.00,
        available = true,
        thumbnailUrl = URI("https://www.stewartbrewing.co.uk/uploads/images/products/large/stewart-brewing-ltd-stewart-brewing-cascadian-east-1589535328cas-front-330.png")
      ),
      ITEMS.byName("Cascadian East")
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

