package watch.craft.scrapers

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import watch.craft.Format.CAN
import watch.craft.Offer
import watch.craft.Scraper.ScrapedItem
import watch.craft.byName
import watch.craft.executeScraper
import watch.craft.noDesc
import java.net.URI

class BurningSkyScraperTest {
  companion object {
    private val ITEMS = executeScraper(BurningSkyScraper(), dateString = "2020-07-15")
  }

  @Test
  fun `finds all the beers`() {
    assertEquals(11, ITEMS.size)
  }

  @Test
  fun `extracts beer details`() {
    assertEquals(
      ScrapedItem(
        name = "Arise",
        abv = 4.4,
        offers = setOf(
          Offer(totalPrice = 3.10, quantity = 1, sizeMl = 440, format = CAN)
        ),
        available = true,
        thumbnailUrl = URI("https://www.burningskybeer.com/wp-content/uploads/2019/11/arise-2019.png")
      ),
      ITEMS.byName("Arise").noDesc()
    )
  }

  @Test
  fun `extracts description`() {
    assertNotNull(ITEMS.byName("Arise").desc)
  }

  @Test
  fun `identifies out-of-stock`() {
    assertFalse(ITEMS.byName("Saison Houblon").available)
  }
}

