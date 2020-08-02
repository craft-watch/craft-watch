package watch.craft.scrapers

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import watch.craft.*
import watch.craft.Format.CAN
import watch.craft.Scraper.Node.ScrapedItem
import java.net.URI

class JeffersonsScraperTest {
  companion object {
    private val ITEMS = executeScraper(JeffersonsScraper(), dateString = "2020-08-02")
  }

  @Test
  fun `finds all the beers`() {
    assertEquals(4, ITEMS.size)
  }

  @Test
  fun `extracts beer details`() {
    assertEquals(
      ScrapedItem(
        name = "The Brightside",
        summary = "Hazy Pale",
        abv = 5.4,
        offers = setOf(
          Offer(totalPrice = 16.80, quantity = 6, sizeMl = 330, format = CAN)
        ),
        available = true,
        thumbnailUrl = URI("https://cdn.shopify.com/s/files/1/2172/2447/products/TheBrightside_Can_200x.jpg")
      ),
      ITEMS.first { it.name == "The Brightside" && it.onlyOffer().quantity == 6 }.noDesc()
    )
  }

  @Test
  fun `extracts description`() {
    assertNotNull(ITEMS.byName("The Brightside").desc)
  }
}

