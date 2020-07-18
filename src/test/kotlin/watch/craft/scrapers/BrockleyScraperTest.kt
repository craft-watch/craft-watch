package watch.craft.scrapers

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import watch.craft.*
import watch.craft.Format.BOTTLE
import watch.craft.Scraper.ScrapedItem
import java.net.URI

class BrockleyScraperTest {
  companion object {
    private val ITEMS = executeScraper(BrockleyScraper(), dateString = "2020-07-18")
  }

  @Test
  fun `finds all the beers`() {
    assertEquals(17, ITEMS.size)
  }

  @Test
  fun `extracts beer details`() {
    assertEquals(
      ScrapedItem(
        name = "Pale Ale",
        abv = 4.1,
        offers = setOf(
          Offer(totalPrice = 14.00, quantity = 6, sizeMl = 330, format = BOTTLE)
        ),
        available = true,
        thumbnailUrl = URI("https://static.wixstatic.com/media/b518ad_eae153e991434a168be20bc1a06cbb1c~mv2.jpg/v1/fill/w_100,h_100,al_c,q_80,usm_0.66_1.00_0.01/b518ad_eae153e991434a168be20bc1a06cbb1c~mv2.jpg")
      ),
      ITEMS.byName("Pale Ale").noDesc()
    )
  }

  @Test
  fun `extracts description`() {
    assertNotNull(ITEMS.byName("Pale Ale").desc)
  }

  @Test
  fun `identifies out-of-stock`() {
    assertFalse(ITEMS.byName("Steppers Extra Stout").available)
  }

  @Test
  fun `identifies mixed`() {
    val item = ITEMS.byName("Brick Lane Beers Mixed Case")

    assertTrue(item.mixed)
    assertTrue(item.onlyOffer().quantity > 1)
  }

  @Test
  fun `cleanses names`() {
    assertFalse(ITEMS.any { it.name.contains("\\d+".toRegex()) })
    assertFalse(ITEMS.any { it.name.contains("keg") })
  }
}

