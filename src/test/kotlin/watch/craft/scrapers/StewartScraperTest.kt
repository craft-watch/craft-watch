package watch.craft.scrapers

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import watch.craft.Item
import watch.craft.byName
import watch.craft.executeScraper
import java.net.URI

class StewartScraperTest {
  companion object {
    private val ITEMS = executeScraper(StewartScraper())
  }

  @Test
  fun `finds all the beers`() {
    assertEquals(14, ITEMS.size)
  }

  @Test
  fun `extracts beer details`() {
    assertEquals(
      Item(
        brewery = "Stewart Brewing",
        name = "First World Problems",
        summary = "India Pale Ale",
        sizeMl = 330,
        abv = 6.2,
        perItemPrice = 1.80,
        available = true,
        url = "https://www.stewartbrewing.co.uk/item/264/StewartBrewing/First-World-Problems.html",
        thumbnailUrl = "https://www.stewartbrewing.co.uk/uploads/images/products/large/stewart-brewing-ltd-stewart-brewing-first-world-problems-1592394080330ml-can-FWP-002-.png"
      ),
      ITEMS.byName("First World Problems")
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

