package choliver.neapi.scrapers

import choliver.neapi.Scraper.Result.Item
import choliver.neapi.executeScraper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import java.net.URI

class FivePointsScraperTest {
  companion object {
    private val ITEMS = executeScraper(FivePointsScraper())
  }

  @Test
  fun `finds all the beers`() {
    assertEquals(10, ITEMS.size)
  }

  @Test
  fun `extracts beer details`() {
    assertEquals(
      Item(
        name = "Five Points Pils",   // No size in title
        summary = "Pilsner",
        perItemPrice = 1.80,
        abv = 4.8,
        sizeMl = 330,
        available = true,
        thumbnailUrl = URI("https://shop.fivepointsbrewing.co.uk/uploads/images/products/large/five-points-brewing-five-points-brewing-five-points-pils-1574871238PILS-Can-Mock-Up.png")
      ),
      ITEMS.first { it.name == "Five Points Pils" }
    )
  }

  @Test
  fun `identifies minikeg`() {
    val item = ITEMS.first { it.name == "Five Points Best" }
    assertEquals("Minikeg", item.summary)
    assertEquals(5000, item.sizeMl)
  }

  @Test
  fun `identifies sold out`() {
    assertFalse(ITEMS.first { it.name == "Five Points Best" }.available)
  }
}

