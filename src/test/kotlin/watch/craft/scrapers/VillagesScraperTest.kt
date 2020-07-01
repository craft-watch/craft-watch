package watch.craft.scrapers

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import watch.craft.Item
import watch.craft.byName
import watch.craft.executeScraper
import watch.craft.noDesc

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
        brewery = "Villages",
        name = "Rodeo", // Normalised case,
        summary = "Pale Ale",
        abv = 4.6,
        sizeMl = 330,
        numItems = 12,
        perItemPrice = 2.13,
        available = true,
        url = "https://villagesbrewery.com/collections/buy-beer/products/rodeo-pale-ale",
        thumbnailUrl = "https://cdn.shopify.com/s/files/1/0360/4735/5948/products/VILLAGES_RODEO_PALE_ALE_330ML_CAN_345x345.jpg"
      ),
      ITEMS.byName("Rodeo").noDesc()
    )
  }

  @Test
  fun `extracts case details`() {
    assertEquals(
      Item(
        brewery = "Villages",
        name = "Villages Mixed Case", // Normalised case
        mixed = true,
        abv = null,   // Can't find this!
        sizeMl = 330,
        numItems = 24,
        perItemPrice = 2.19,
        available = true,
        url = "https://villagesbrewery.com/collections/buy-beer/products/villages-mixed-case",
        thumbnailUrl = "https://cdn.shopify.com/s/files/1/0360/4735/5948/products/VILLAGES_MIXED_CASE_345x345.jpg"
      ),
      ITEMS.byName("Villages Mixed Case").noDesc()
    )
  }

  @Test
  fun `sanitises description`() {
    assertFalse(ITEMS.byName("Rodeo").desc!!.contains("~"))
  }
}

