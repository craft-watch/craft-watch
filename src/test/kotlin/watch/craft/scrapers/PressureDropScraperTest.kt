package watch.craft.scrapers

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import watch.craft.Scraper.Item
import watch.craft.byName
import watch.craft.executeScraper
import watch.craft.noDesc
import java.net.URI

class PressureDropScraperTest {
  companion object {
    private val ITEMS = executeScraper(PressureDropScraper())
  }

  @Test
  fun `finds all the beers`() {
    assertEquals(9, ITEMS.size)
  }

  @Test
  fun `extracts beer details`() {
    assertEquals(
      Item(
        name = "Golden State",
        summary = "New England Pale",
        sizeMl = 440,
        abv = 5.2,
        perItemPrice = 4.05,
        available = true,
        thumbnailUrl = URI("https://cdn.shopify.com/s/files/1/0173/0153/6832/products/IMG_9751_large.jpg?v=1592315629")
      ),
      ITEMS.byName("Golden State").noDesc()
    )
  }

  @Test
  fun `ignores boxes`() {
    assertEquals(
      emptyList<String>(),
      ITEMS.map { it.name }.filter { it.contains("box", ignoreCase = true) }
    )
  }

  @Test
  fun `extracts description`() {
    assertNotNull(ITEMS.byName("Golden State").desc)
  }
}

