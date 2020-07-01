package watch.craft.scrapers

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import watch.craft.Item
import watch.craft.byName
import watch.craft.executeScraper
import watch.craft.noDesc

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
        brewery = "Five Points",
        name = "Five Points Pils",   // No size in title
        summary = "Pilsner",
        numItems = 12,
        perItemPrice = 1.80,
        abv = 4.8,
        sizeMl = 330,
        available = true,
        url = "https://shop.fivepointsbrewing.co.uk/item/8/FivePointsBrewing/FIVE-POINTS-PILS-12-PACK.html",
        thumbnailUrl = "https://shop.fivepointsbrewing.co.uk/uploads/images/products/large/five-points-brewing-five-points-brewing-five-points-pils-1574871238PILS-Can-Mock-Up.png"
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
  @Disabled
  fun `identifies sold out`() {
    assertFalse(ITEMS.byName("Five Points Best").available)
  }

  @Test
  fun `extracts description`() {
    assertNotNull(ITEMS.byName("Five Points Pils").desc)
  }
}

