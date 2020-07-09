package watch.craft.scrapers

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import watch.craft.*
import watch.craft.Scraper.ScrapedItem
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
      ScrapedItem(
        name = "Five Points Pils",   // No size in title
        summary = "Pilsner",
        offers = setOf(
          Offer(quantity = 12, totalPrice = 21.60, sizeMl = 330)
        ),
        abv = 4.8,
        available = true,
        thumbnailUrl = URI("https://shop.fivepointsbrewing.co.uk/uploads/images/products/large/five-points-brewing-five-points-brewing-five-points-pils-1574871238PILS-Can-Mock-Up.png")
      ),
      ITEMS.byName("Five Points Pils").noDesc()
    )
  }

  @Test
  fun `identifies minikeg`() {
    val item = ITEMS.byName("Five Points Best")
    assertTrue(item.onlyOffer().keg)
    assertEquals(5000, item.onlyOffer().sizeMl)
  }

  @Test
  @Disabled
  fun `identifies sold out`() {
    assertFalse(ITEMS.byName("Five Points Best").available)
  }

  @Test
  fun `extracts description`() {
    assertNotNull(ITEMS.byName("Five Points Pils").desc)
  }
}

