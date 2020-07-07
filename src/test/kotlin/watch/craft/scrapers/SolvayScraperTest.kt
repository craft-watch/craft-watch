package watch.craft.scrapers

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import watch.craft.Scraper.ScrapedItem
import watch.craft.byName
import watch.craft.executeScraper
import watch.craft.noDesc
import java.net.URI

class SolvayScraperTest {
  companion object {
    private val ITEMS = executeScraper(SolvayScraper(), dateString = null)
  }

  @Test
  fun `finds all the beers`() {
    ITEMS.forEach { println(it.noDesc()) }
//    assertEquals(2, ITEMS.size)
  }

//  @Test
//  fun `extracts beer details`() {
//    assertEquals(
//      ScrapedItem(
//        name = "Lulla",
//        abv = 3.5,
//        sizeMl = 440,
//        price = 3.99,
//        available = true,
//        thumbnailUrl = URI("https://cdn.shopify.com/s/files/1/0286/3471/0061/products/Cans_800x800_crop_center.jpg?v=1593009635")
//      ),
//      ITEMS.byName("Lulla").noDesc()
//    )
//  }
//
//  @Test
//  fun `extracts description`() {
//    assertNotNull(ITEMS.byName("Lulla").desc)
//  }
//
//  @Test
//  fun `identifies multi-packs`() {
//    assertEquals(6, ITEMS.byName("Parade").numItems)
//  }
//
//  @Test
//  fun `identifies sold out`() {
//    assertFalse(ITEMS.byName("Parade").available)
//  }
}

