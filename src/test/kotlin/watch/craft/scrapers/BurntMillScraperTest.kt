package watch.craft.scrapers

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import watch.craft.*
import watch.craft.Format.CAN
import watch.craft.Scraper.Node.ScrapedItem
import watch.craft.dsl.containsWord
import java.net.URI

class BurntMillScraperTest {
  companion object {
    private val ITEMS = executeScraper(BurntMillScraper(), dateString = "2020-08-08")
  }

  @Test
  fun `finds all the beers`() {
    assertEquals(6, ITEMS.size)
  }

  @Test
  fun `extracts beer details`() {
    assertEquals(
      ScrapedItem(
        name = "Bitter Lake",
        abv = 5.5,
        offers = setOf(
          Offer(quantity = 1, totalPrice = 3.95, sizeMl = 440, format = CAN)
        ),
        available = true,
        thumbnailUrl = URI("https://cdn.shopify.com/s/files/1/0276/1054/6273/products/aqEjZMAOTKG71KB9qL3D_bitterlake_200x.jpg")
      ),
      ITEMS.byName("Bitter Lake").noDesc()
    )
  }

  @Test
  fun `extracts description`() {
    assertNotNull(ITEMS.byName("Bitter Lake").desc)
  }

  @Test
  fun `identifies sold-out`() {
    assertFalse(ITEMS.byName("Pintle Pale Ale").available)
  }

  @Test
  fun `identifies mixed`() {
    val item = ITEMS.byName("Pale Ale Mixed")

    assertTrue(item.mixed)
    assertTrue(item.onlyOffer().quantity == 12)
  }

  @Test
  fun `cleans up names`() {
    assertFalse(ITEMS.any { it.name.contains("%") })
    assertFalse(ITEMS.any { it.name.contains("ml", ignoreCase = true) })
    assertFalse(ITEMS.any { it.name.containsWord("pack") })
  }
}

