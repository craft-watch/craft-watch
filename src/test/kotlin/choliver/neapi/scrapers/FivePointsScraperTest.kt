package choliver.neapi.scrapers

import choliver.neapi.Scraper.Item
import choliver.neapi.byName
import choliver.neapi.executeScraper
import choliver.neapi.noDesc
import org.junit.jupiter.api.Assertions.*
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
      ITEMS.byName("Five Points Pils").noDesc()
    )
  }

  @Test
  fun `identifies minikeg`() {
    val item = ITEMS.byName("Five Points Best")
    assertTrue(item.keg)
    assertEquals(5000, item.sizeMl)
  }

  @Test
  fun `identifies sold out`() {
    assertFalse(ITEMS.byName("Five Points Best").available)
  }

  @Test
  fun `extracts description`() {
    assertNotNull(ITEMS.byName("Five Points Pils").desc)
  }
}

