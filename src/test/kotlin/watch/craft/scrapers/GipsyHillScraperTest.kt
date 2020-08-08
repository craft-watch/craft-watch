package watch.craft.scrapers

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import watch.craft.*
import watch.craft.Scraper.Node.ScrapedItem
import java.net.URI

class GipsyHillScraperTest {
  companion object {
    private val ITEMS = executeScraper(GipsyHillScraper(), dateString = "2020-08-08")
  }

  @Test
  fun `finds all the beers`() {
    ITEMS.display()
    assertEquals(20, ITEMS.size)
  }

  @Test
  fun `extracts beer details`() {
    assertEquals(
      ScrapedItem(
        name = "Carver",
        summary = "Micro IPA",
        offers = setOf(
          Offer(totalPrice = 2.20, sizeMl = 330, format = Format.CAN)
        ),
        abv = 2.8,
        available = true,
        thumbnailUrl = URI("https://i2.wp.com/shop.gipsyhillbrew.com/wp-content/uploads/2020/04/CARVER.jpg?fit=644%2C800&#038;ssl=1")
      ),
      ITEMS.byName("Carver").noDesc()
    )
  }

  @Test
  fun `extracts description`() {
    assertNotNull(ITEMS.byName("Carver").desc)
  }

  @Test
  fun `identifies sold-out`() {
    assertFalse(ITEMS.byName("Dinomania").available)
  }
}

