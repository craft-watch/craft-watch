package watch.craft.scrapers

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import watch.craft.*
import watch.craft.Format.CAN
import watch.craft.Scraper.ScrapedItem
import java.net.URI

class WildCardScraperTest {
  companion object {
    private val ITEMS = executeScraper(WildCardScraper(), dateString = "2020-07-23")
  }

  @Test
  fun `finds all the beers`() {
    assertEquals(15, ITEMS.size)
  }

  @Test
  fun `extracts beer details`() {
    assertEquals(
      ScrapedItem(
        name = "Cashmere IPA",
        abv = 5.8,
        offers = setOf(
          Offer(quantity = 12, totalPrice = 45.00, sizeMl = 440, format = CAN),
          Offer(quantity = 24, totalPrice = 85.00, sizeMl = 440, format = CAN)
        ),
        available = true,
        thumbnailUrl = URI("https://cdn.shopify.com/s/files/1/0382/3671/7188/products/Cashmere_IPA_250x250.jpg")
      ),
      ITEMS.byName("Cashmere IPA").noDesc()
    )
  }

  @Test
  fun `extracts description`() {
    assertNotNull(ITEMS.byName("Cashmere IPA").desc)
  }

  @Test
  fun `identifies mixed`() {
    assertTrue(ITEMS.byName("IPA Box").mixed)
    assertTrue(ITEMS.byName("Mixed Core IPA + Pale").mixed)
  }

  @Test
  fun `cleans up names`() {
    assertFalse(ITEMS.any { it.name.contains("\\d".toRegex()) })
    assertFalse(ITEMS.any { it.name.contains("fresh", ignoreCase = true) })
  }

  @Test
  fun `extracts offer when no useful JSON-LD`() {
    assertEquals(
      Offer(quantity = 12, totalPrice = 36.50, format = CAN),
      ITEMS.byName("Walthamstow Garden Party - Tasting Box").onlyOffer()
    )
  }
}

