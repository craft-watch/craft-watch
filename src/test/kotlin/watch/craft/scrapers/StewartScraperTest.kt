package watch.craft.scrapers

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import watch.craft.Item
import watch.craft.byName
import watch.craft.executeScraper

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
      Item(
        brewery = "Stewart Brewing",
        name = "St Giles",
        summary = "Scotch Ale",
        sizeMl = 330,
        abv = 5.0,
        perItemPrice = 1.80,
        available = true,
        url = "https://www.stewartbrewing.co.uk/item/254/StewartBrewing/St-Giles.html",
        thumbnailUrl = "https://www.stewartbrewing.co.uk/uploads/images/products/large/stewart-brewing-ltd-stewart-brewing-st-giles-1592389432330ml-StGiles.png"
      ),
      ITEMS.byName("St Giles")
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

