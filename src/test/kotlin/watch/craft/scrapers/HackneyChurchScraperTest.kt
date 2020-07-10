package watch.craft.scrapers

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import watch.craft.*
import watch.craft.Scraper.ScrapedItem
import java.net.URI

class HackneyChurchScraperTest {
  companion object {
    private val ITEMS = executeScraper(HackneyChurchScraper())
  }

  @Test
  fun `finds all the beers`() {
    assertEquals(6, ITEMS.size)
  }

  @Test
  fun `extracts beer details`() {
    assertEquals(
      ScrapedItem(
        name = "Superfly IPA",
        abv = null,   // Not listed
        offers = setOf(
          Offer(totalPrice = 36.00, quantity = 12, sizeMl = 330)
        ),
        available = true,
        thumbnailUrl = URI("https://cdn.shopify.com/s/files/1/0267/3842/5904/products/IMG_26312copy_200x.png?v=1593030329")
      ),
      ITEMS.byName("Superfly IPA").noDesc()
    )
  }

  @Test
  fun `extracts description`() {
    assertNotNull(ITEMS.byName("Superfly IPA").desc)
  }

  @Test
  fun `identifies mixed packs`() {
    val item = ITEMS.byName("Mixed Case")

    assertTrue(item.mixed)
    assertTrue(item.onlyOffer().quantity > 1)
  }

  @Test
  fun `identifies sold out`() {
    assertFalse(ITEMS.byName("Session with Ry IPA").available)
  }
}

