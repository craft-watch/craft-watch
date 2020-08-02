package watch.craft.scrapers

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import watch.craft.*
import watch.craft.Scraper.Node.ScrapedItem
import java.net.URI

class WanderScraperTest {
  companion object {
    private val ITEMS = executeScraper(WanderScraper(), dateString = "2020-08-02")
  }

  @Test
  fun `finds all the beers`() {
    assertEquals(16, ITEMS.size)
  }

  @Test
  fun `extracts beer details`() {
    assertEquals(
      ScrapedItem(
        name = "Lutra Lutra",
        summary = "Pale Ale",
        abv = 5.0,
        offers = setOf(
          Offer(totalPrice = 3.50, sizeMl = 440)
        ),
        available = true,
        thumbnailUrl = URI("https://static.wixstatic.com/media/f0be53_e2d8b9df0b1944b18b6e71c20510e0a4~mv2.jpg/v1/fill/w_100,h_100,al_c,q_80,usm_0.66_1.00_0.01/f0be53_e2d8b9df0b1944b18b6e71c20510e0a4~mv2.jpg")
      ),
      ITEMS.byName("Lutra Lutra").noDesc()
    )
  }

  @Test
  fun `extracts description`() {
    assertNotNull(ITEMS.byName("Lutra Lutra").desc)
  }

  @Test
  fun `identifies mixed cases`() {
    val item = ITEMS.byName("Vegan Mixed Case")
    assertTrue(item.mixed)
    assertEquals(12, item.onlyOffer().quantity)
  }
}

