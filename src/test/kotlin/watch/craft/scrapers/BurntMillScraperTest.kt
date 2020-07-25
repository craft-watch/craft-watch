package watch.craft.scrapers

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import watch.craft.Format.CAN
import watch.craft.Offer
import watch.craft.Scraper.Node.ScrapedItem
import watch.craft.executeScraper
import java.net.URI

class BurntMillScraperTest {
  companion object {
    private val ITEMS = executeScraper(BurntMillScraper(), dateString = "2020-07-23")
  }

  @Test
  fun `finds all the beers`() {
    assertEquals(8, ITEMS.size)
  }

  @Test
  fun `extracts beer details`() {
    assertEquals(
      ScrapedItem(
        name = "Strata Fog IPA",
        abv = 6.2,
        offers = setOf(
          Offer(quantity = 1, totalPrice = 4.55, sizeMl = 440, format = CAN)
        ),
        available = true,
        thumbnailUrl = URI("https://cdn.shopify.com/s/files/1/0276/1054/6273/products/vGKdDTxbSgK8b3ij0vTQ_StrataFog_200x.jpg")
      ),
      ITEMS.byName("Strata Fog IPA").noDesc()
    )
  }

  @Test
  fun `extracts description`() {
    assertNotNull(ITEMS.byName("Strata Fog IPA").desc)
  }

  @Test
  fun `identifies sold-out`() {
    assertFalse(ITEMS.byName("The Weight Of Brunch").available)
  }

  @Test
  fun `cleans up names`() {
    assertFalse(ITEMS.any { it.name.contains("%") })
    assertFalse(ITEMS.any { it.name.contains("ml", ignoreCase = true) })
    assertFalse(ITEMS.any { it.name.contains("pack", ignoreCase = true) })
  }
}

