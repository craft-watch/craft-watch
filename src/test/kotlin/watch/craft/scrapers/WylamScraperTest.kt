package watch.craft.scrapers

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import watch.craft.Offer
import watch.craft.Scraper.Node.ScrapedItem
import watch.craft.byName
import watch.craft.executeScraper
import watch.craft.noDesc
import java.net.URI

class WylamScraperTest {
  companion object {
    private val ITEMS = executeScraper(WylamScraper())
  }

  @Test
  fun `finds all the beers`() {
    assertEquals(12, ITEMS.size)
  }

  @Test
  fun `extracts beer details`() {
    assertEquals(
      ScrapedItem(
        name = "Wylam Gold",
        summary = "English Golden Ale",
        abv = 4.0,
        offers = setOf(
          Offer(totalPrice = 3.50, sizeMl = 440)
        ),
        available = true,
        thumbnailUrl = URI("https://dpbfm6h358sh7.cloudfront.net/images/26164003/1393806261.jpg")
      ),
      ITEMS.byName("Wylam Gold").noDesc()
    )
  }

  @Test
  fun `extracts description`() {
    assertNotNull(ITEMS.byName("Wylam Gold").desc)
  }

  @Test
  fun `ignores non-beers`() {
    assertFalse(ITEMS.any { it.name.contains("glass", ignoreCase = true) })
  }
}

