package watch.craft.scrapers

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import watch.craft.*
import watch.craft.Format.KEG
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
        name = "Five Points Jupa",   // No size in title
        summary = "Juicy Pale Ale",
        offers = setOf(
          Offer(quantity = 12, totalPrice = 23.50, sizeMl = 330)
        ),
        abv = 5.5,
        available = true,
        thumbnailUrl = URI("https://shop.fivepointsbrewing.co.uk/uploads/images/products/large/five-points-brewing-five-points-brewing-five-points-jupa-1574871172Jupa-Can-Mock-Up.png")
      ),
      ITEMS.byName("Five Points Jupa").noDesc()
    )
  }

  @Test
  fun `identifies minikeg`() {
    val item = ITEMS.byName("Five Points Best")
    assertEquals(KEG, item.onlyOffer().format)
    assertEquals(5000, item.onlyOffer().sizeMl)
  }

  @Test
  fun `identifies sold out`() {
    assertFalse(ITEMS.byName("Five Points Pils").available)
  }

  @Test
  fun `extracts description`() {
    assertNotNull(ITEMS.byName("Five Points Pils").desc)
  }
}

