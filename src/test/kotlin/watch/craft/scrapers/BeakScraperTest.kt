package watch.craft.scrapers

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import watch.craft.*
import watch.craft.Scraper.ScrapedItem
import java.net.URI

class BeakScraperTest {
  companion object {
    private val ITEMS = executeScraper(BeakScraper())
  }

  @Test
  fun `finds all the beers`() {
    assertEquals(2, ITEMS.size)
  }

  @Test
  fun `extracts beer details`() {
    assertEquals(
      ScrapedItem(
        name = "Lulla",
        abv = 3.5,
        offers = setOf(
          Offer(totalPrice = 3.99, sizeMl = 440)
        ),
        available = false,
        thumbnailUrl = URI("https://cdn.shopify.com/s/files/1/0286/3471/0061/products/Cans_800x800_crop_center.jpg?v=1593009635")
      ),
      ITEMS.byName("Lulla").noDesc()
    )
  }

  @Test
  fun `extracts description`() {
    assertNotNull(ITEMS.byName("Lulla").desc)
  }

  @Test
  fun `identifies multi-packs`() {
    assertEquals(6, ITEMS.byName("Parade").onlyOffer().quantity)
  }

  @Test
  fun `identifies sold out`() {
    assertFalse(ITEMS.byName("Parade").available)
  }
}

