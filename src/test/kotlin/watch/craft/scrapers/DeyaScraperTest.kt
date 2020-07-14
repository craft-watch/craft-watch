package watch.craft.scrapers

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import watch.craft.*
import watch.craft.Format.CAN
import watch.craft.Scraper.ScrapedItem
import java.net.URI

class DeyaScraperTest {
  companion object {
    private val ITEMS = executeScraper(DeyaScraper(), dateString = "2020-07-14")
  }

  @Test
  fun `finds all the beers`() {
    assertEquals(6, ITEMS.size)
  }

  @Test
  fun `extracts beer details`() {
    assertEquals(
      ScrapedItem(
        name = "Steady Rolling Man",    // "6 pack" removed
        abv = 5.2,
        offers = setOf(
          Offer(quantity = 6, totalPrice = 20.00, sizeMl = 500, format = CAN)
        ),
        available = true,
        thumbnailUrl = URI("https://craftpeak-commerce-images.imgix.net/2019/06/SteadyRollingManClip.jpg?auto=compress%2Cformat&fit=crop&h=324&ixlib=php-1.2.1&w=324&wpsize=woocommerce_thumbnail")
      ),
      ITEMS.byName("Steady Rolling Man").noDesc()
    )
  }

  @Test
  fun `extracts description`() {
    assertNotNull(ITEMS.byName("Steady Rolling Man").desc)
  }

  @Test
  fun `identifies mixed`() {
    val items = ITEMS.filter { it.mixed }

    assertFalse(items.isEmpty())
    assertFalse(items.any { it.onlyOffer().quantity == 1 })
  }
}

