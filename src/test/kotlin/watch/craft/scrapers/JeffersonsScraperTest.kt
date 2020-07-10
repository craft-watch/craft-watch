package watch.craft.scrapers

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import watch.craft.*
import watch.craft.Scraper.ScrapedItem
import java.net.URI

class JeffersonsScraperTest {
  companion object {
    private val ITEMS = executeScraper(JeffersonsScraper(), dateString = null)
  }

  @Test
  fun `finds all the beers`() {
    assertEquals(4, ITEMS.size)
  }

  @Test
  fun `extracts beer details`() {
    assertEquals(
      ScrapedItem(
        name = "Birthday No.3",
        summary = "New England Pale",
        abv = 5.4,
        offers = setOf(
          Offer(totalPrice = 22.50, quantity = 6, sizeMl = 440)
        ),
        available = true,
        thumbnailUrl = URI("https://cdn.shopify.com/s/files/1/2172/2447/products/BirthdayNo.3_300x300.jpg?v=1594224138")
      ),
      ITEMS.first { it.name == "Birthday No.3" && it.onlyOffer().quantity == 6 }.noDesc()
    )
  }

  @Test
  fun `extracts description`() {
    assertNotNull(ITEMS.byName("Birthday No.3").desc)
  }
}

