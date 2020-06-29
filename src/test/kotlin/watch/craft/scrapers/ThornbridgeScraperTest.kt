package watch.craft.scrapers

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import watch.craft.Scraper.Item
import watch.craft.byName
import watch.craft.executeScraper
import watch.craft.noDesc
import java.net.URI

class ThornbridgeScraperTest {
  companion object {
    private val ITEMS = executeScraper(ThornbridgeScraper(), dateString = null) //"2020-06-29")
  }

  @Test
  fun `finds all the beers`() {
    ITEMS.forEach { println(it.noDesc()) }
    assertEquals(30, ITEMS.size)
  }

  @Test
  fun `extracts beer details`() {
    assertEquals(
      Item(
        name = "Jaipur",    // No "can" or "bottle" in title
        summary = "IPA",
        sizeMl = 330,
        abv = 5.9,
        perItemPrice = 2.00,
        available = true,
        thumbnailUrl = URI("https://cdn.shopify.com/s/files/1/0075/9939/0831/products/Jaipur_bottle_mockup_for_website_300x300.jpg?v=1583153726")
      ),
      ITEMS.byName("Jaipur").noDesc()
    )
  }

  @Test
  fun `extracts description`() {
    assertNotNull(ITEMS.byName("Jaipur").desc)
  }

  @Test
  fun `ignores things that aren't beers`() {
    assertFalse(ITEMS.any { it.name.contains("glass", ignoreCase = true) })
  }
}

