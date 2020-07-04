package watch.craft.scrapers

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import watch.craft.Scraper.ScrapedItem
import watch.craft.byName
import watch.craft.executeScraper
import watch.craft.noDesc
import java.net.URI

class WanderScraperTest {
  companion object {
    private val ITEMS = executeScraper(WanderScraper(), dateString = null)
  }

  @Test
  fun `finds all the beers`() {
    ITEMS.forEach(::println)
//    assertEquals(3, ITEMS.size)
  }

//  @Test
//  fun `extracts beer details`() {
//    assertEquals(
//      ScrapedItem(
//        name = "Conflux Pale Ale",
//        abv = 4.8,
//        sizeMl = 440,
//        price = 3.80,
//        available = true,
//        thumbnailUrl = URI("https://cdn.shopify.com/s/files/1/0340/2829/0107/products/CONFLUX_can_345x345.jpg")
//      ),
//      ITEMS.byName("Conflux Pale Ale").noDesc()
//    )
//  }
//
//  @Test
//  fun `sanitises description`() {
//    assertFalse(ITEMS.byName("Conflux Pale Ale").desc!!.contains("~"))
//  }
}

