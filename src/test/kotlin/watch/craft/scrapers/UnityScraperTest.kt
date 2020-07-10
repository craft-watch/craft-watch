package watch.craft.scrapers

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import watch.craft.Offer
import watch.craft.Scraper.ScrapedItem
import watch.craft.byName
import watch.craft.executeScraper
import watch.craft.noDesc
import java.net.URI

class UnityScraperTest {
  companion object {
    private val ITEMS = executeScraper(UnityScraper())
  }

  @Test
  fun `finds all the beers`() {
    assertEquals(5, ITEMS.size)
  }

  @Test
  fun `extracts beer details`() {
    assertEquals(
      ScrapedItem(
        name = "Conflux Pale Ale",
        abv = 4.8,
        offers = setOf(
          Offer(totalPrice = 3.80, sizeMl = 440)
        ),
        available = true,
        thumbnailUrl = URI("https://cdn.shopify.com/s/files/1/0340/2829/0107/products/CONFLUX_can_345x345.jpg?v=1584098753")
      ),
      ITEMS.byName("Conflux Pale Ale").noDesc()
    )
  }

  @Test
  fun `extracts description`() {
    assertNotNull(ITEMS.byName("Conflux Pale Ale").desc)
  }
}

