package watch.craft.scrapers

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import watch.craft.Offer
import watch.craft.Scraper.ScrapedItem
import watch.craft.byName
import watch.craft.executeScraper
import watch.craft.noDesc
import java.net.URI

class OrbitScraperTest {
  companion object {
    private val ITEMS = executeScraper(OrbitScraper(), dateString = "2020-07-12")
  }

  @Test
  fun `finds all the beers`() {
    assertEquals(12, ITEMS.size)
  }

  @Test
  fun `extracts beer details`() {
    assertEquals(
      ScrapedItem(
        name = "Ivo Pale Ale",
        abv = 4.5,
        offers = setOf(
          Offer(quantity = 12, totalPrice = 28.80, sizeMl = 330),
          Offer(quantity = 24, totalPrice = 54.00, sizeMl = 330)
        ),
        available = true,
        thumbnailUrl = URI("https://cdn.shopify.com/s/files/1/0360/1235/9817/products/IvoAmazon_250x250@2x.jpg")
      ),
      ITEMS.byName("Ivo Pale Ale").noDesc()
    )
  }

  @Test
  fun `extracts description`() {
    assertNotNull(ITEMS.byName("Ivo Pale Ale").desc)
  }

  @Test
  fun `identifies mixed packs`() {
    assertFalse(ITEMS.none { it.mixed })
  }

  @Test
  fun `identifies sold-out`() {
    assertFalse(ITEMS.byName("Hefeweizen").available)
  }

  @Test
  fun `ignores non-beers`() {
    assertFalse(ITEMS.any { it.name.contains("shirt", ignoreCase = true) })
  }
}

