package watch.craft.scrapers

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import watch.craft.Format.CAN
import watch.craft.Offer
import watch.craft.Scraper.ScrapedItem
import watch.craft.byName
import watch.craft.executeScraper
import watch.craft.noDesc
import java.net.URI

class VillagesScraperTest {
  companion object {
    private val ITEMS = executeScraper(VillagesScraper())
  }

  @Test
  fun `finds all the beers`() {
    assertEquals(7, ITEMS.size)
  }

  @Test
  fun `extracts beer details`() {
    assertEquals(
      ScrapedItem(
        name = "Rodeo", // Normalised case,
        summary = "Pale Ale",
        abv = 4.6,
        offers = setOf(
          Offer(quantity = 12, totalPrice = 25.60, sizeMl = 330, format = CAN),
          Offer(quantity = 24, totalPrice = 49.20, sizeMl = 330, format = CAN)
        ),
        available = true,
        thumbnailUrl = URI("https://cdn.shopify.com/s/files/1/0360/4735/5948/products/VILLAGES_RODEO_PALE_ALE_330ML_CAN_345x345.jpg")
      ),
      ITEMS.byName("Rodeo").noDesc()
    )
  }

  @Test
  fun `extracts case details`() {
    assertEquals(
      ScrapedItem(
        name = "Villages Mixed Case", // Normalised case
        mixed = true,
        abv = null,   // Can't find this!
        offers = setOf(
          Offer(quantity = 24, totalPrice = 52.50, sizeMl = 330, format = CAN)
        ),
        available = true,
        thumbnailUrl = URI("https://cdn.shopify.com/s/files/1/0360/4735/5948/products/VILLAGES_MIXED_CASE_345x345.jpg")
      ),
      ITEMS.byName("Villages Mixed Case").noDesc()
    )
  }

  @Test
  fun `extracts description`() {
    assertNotNull(ITEMS.byName("Rodeo").desc)
  }
}

