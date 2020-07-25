package watch.craft.scrapers

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import watch.craft.*
import watch.craft.Scraper.Output.ScrapedItem
import java.net.URI

class CloudwaterScraperTest {
  companion object {
    private val ITEMS = executeScraper(CloudwaterScraper(), dateString = "2020-07-16")
  }

  @Test
  fun `finds all the beers`() {
    assertEquals(22, ITEMS.size)
  }

  @Test
  fun `extracts beer details`() {
    assertEquals(
      ScrapedItem(
        name = "Bird Tweets Trump Trump Tweets",
        summary = "DDH Pale",
        abv = 5.0,
        offers = setOf(
          Offer(totalPrice = 4.75, sizeMl = 440)
        ),
        available = true,
        thumbnailUrl = URI("https://cdn.shopify.com/s/files/1/0088/5076/7952/products/GyleNo.873_1_800x.jpg")
      ),
      ITEMS.byName("Bird Tweets Trump Trump Tweets").noDesc()
    )
  }

  @Test
  fun `extracts description`() {
    assertNotNull(ITEMS.byName("Bird Tweets Trump Trump Tweets").desc)
  }

  @Test
  fun `identifies mixed`() {
    val items = ITEMS.filter { it.mixed }

    assertFalse(items.isEmpty())
    assertFalse(items.any { it.onlyOffer().quantity == 1 })
    assertFalse(items.any { it.onlyOffer().sizeMl != null })
    assertFalse(items.any { it.abv != null })
  }

  @Test
  fun `identifies quantities`() {
    assertEquals(6, ITEMS.byName("All Together 6 Pack").onlyOffer().quantity)
  }
}

