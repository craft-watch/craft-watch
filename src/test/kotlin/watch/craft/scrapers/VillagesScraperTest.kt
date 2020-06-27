package watch.craft.scrapers

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import watch.craft.Scraper.Item
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
      Item(
        name = "Rodeo", // Normalised case,
        summary = "Pale Ale",
        abv = 4.6,
        sizeMl = 330,
        perItemPrice = 2.13,
        available = true,
        thumbnailUrl = URI("https://cdn.shopify.com/s/files/1/0360/4735/5948/products/VILLAGES_RODEO_PALE_ALE_330ML_CAN_345x345.jpg")
      ),
      ITEMS.byName("Rodeo").noDesc()
    )
  }

  @Test
  fun `extracts case details`() {
    assertEquals(
      Item(
        name = "Villages Mixed Case", // Normalised case
        summary = "24 cans",  // Synthesised summary
        mixed = true,
        abv = null,   // Can't find this!
        sizeMl = 330,
        perItemPrice = 2.19,
        available = true,
        thumbnailUrl = URI("https://cdn.shopify.com/s/files/1/0360/4735/5948/products/VILLAGES_MIXED_CASE_345x345.jpg")
      ),
      ITEMS.byName("Villages Mixed Case").noDesc()
    )
  }

  @Test
  fun `sanitises description`() {
    assertFalse(ITEMS.byName("Rodeo").desc!!.contains("~"))
  }
}

