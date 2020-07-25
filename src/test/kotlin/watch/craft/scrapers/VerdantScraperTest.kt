package watch.craft.scrapers

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import watch.craft.Offer
import watch.craft.Scraper.Output.ScrapedItem
import watch.craft.byName
import watch.craft.executeScraper
import watch.craft.noDesc
import java.net.URI

class VerdantScraperTest {
  companion object {
    private val ITEMS = executeScraper(VerdantScraper(), dateString = "2020-07-16")
  }

  @Test
  fun `finds all the beers`() {
    assertEquals(5, ITEMS.size)
  }

  @Test
  fun `extracts beer details`() {
    assertEquals(
      ScrapedItem(
        name = "Some Fifty Pale Ale",
        summary = "Pale Ale",
        abv = 5.2,
        offers = setOf(
          Offer(quantity = 6, totalPrice = 21.00, sizeMl = 440)
        ),
        available = false,
        thumbnailUrl = URI("https://cdn.shopify.com/s/files/1/1960/0337/products/Some50-Product2_100x.jpg")
      ),
      ITEMS.byName("Some Fifty Pale Ale").noDesc()
    )
  }

  @Test
  fun `extracts description`() {
    assertNotNull(ITEMS.byName("Some Fifty Pale Ale").desc)
  }

  @Test
  fun `identifies mixed packs`() {
    assertFalse(ITEMS.none { it.mixed })
  }

  @Test
  fun `ignores out-of-stock`() {
    assertFalse(ITEMS.byName("Pilsner Mixed").available)
  }

  @Test
  fun `ignores non-beers`() {
    assertFalse(ITEMS.any { it.name.contains("gift") })
  }
}

