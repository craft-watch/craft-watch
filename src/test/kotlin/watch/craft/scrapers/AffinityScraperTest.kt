package watch.craft.scrapers

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import watch.craft.*
import watch.craft.Format.BOTTLE
import watch.craft.Format.CAN
import watch.craft.Scraper.ScrapedItem
import java.net.URI

class AffinityScraperTest {
  companion object {
    private val ITEMS = executeScraper(AffinityScraper(), dateString = null)
  }

  @Test
  fun `finds all the beers`() {
    ITEMS.display()
//    assertEquals(12, ITEMS.size)
  }

//  @Test
//  fun `extracts beer details`() {
//    assertEquals(
//      ScrapedItem(
//        name = "Pine Trail",
//        summary = "Pale Ale",
//        abv = 0.5,
//        offers = setOf(
//          Offer(quantity = 4, totalPrice = 6.40, sizeMl = 330, format = CAN),
//          Offer(quantity = 12, totalPrice = 21.00, sizeMl = 330, format = BOTTLE)
//        ),
//        available = true,
//        thumbnailUrl = URI("https://cdn.shopify.com/s/files/1/0348/1675/3803/products/shop-can-set-pale_300x.jpg")
//      ),
//      ITEMS.byName("Pine Trail").noDesc()
//    )
//  }
//
//  @Test
//  fun `extracts description`() {
//    assertNotNull(ITEMS.byName("Pine Trail").desc)
//  }
//
//  @Test
//  fun `identifies sold-out and falls back to basic price info`() {
//    val item = ITEMS.byName("BST")
//
//    assertFalse(item.available)
//    assertEquals(
//      Offer(quantity = 12, totalPrice = 19.00, sizeMl = 330, format = BOTTLE),
//      item.onlyOffer()
//    )
//  }
//
//  @Test
//  fun `identifies mixed`() {
//    assertTrue(ITEMS.byName("Big Drop Brewing").mixed)
//  }
}

