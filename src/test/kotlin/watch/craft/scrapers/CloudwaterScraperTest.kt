package watch.craft.scrapers

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import watch.craft.Scraper.ScrapedItem
import watch.craft.byName
import watch.craft.executeScraper
import watch.craft.noDesc
import java.net.URI

class CloudwaterScraperTest {
  companion object {
    private val ITEMS = executeScraper(CloudwaterScraper(), dateString = "2020-07-07")
  }

  @Test
  fun `finds all the beers`() {
    assertEquals(24, ITEMS.size)
  }

  @Test
  fun `extracts beer details`() {
    assertEquals(
      ScrapedItem(
        name = "Bird Tweets Trump Trump Tweets",
        summary = "DDH Pale",
        abv = 5.0,
        sizeMl = 440,
        price = 4.75,
        available = true,
        thumbnailUrl = URI("https://cdn.shopify.com/s/files/1/0088/5076/7952/products/GyleNo.873_1_800x.jpg?v=1591967414")
      ),
      ITEMS.byName("Bird Tweets Trump Trump Tweets").noDesc()
    )
  }

  @Test
  fun `extracts description`() {
    assertNotNull(ITEMS.byName("DDH Pale").desc)
  }

  @Test
  fun `identifies mixed`() {
    val items = ITEMS.filter { it.mixed }

    assertFalse(items.isEmpty())
    assertFalse(items.any { it.numItems == 1})
    assertFalse(items.any { it.sizeMl != null })
    assertFalse(items.any { it.abv != null })
  }
}

