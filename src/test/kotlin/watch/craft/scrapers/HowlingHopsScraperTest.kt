package watch.craft.scrapers

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import watch.craft.Offer
import watch.craft.Scraper.ScrapedItem
import watch.craft.byName
import watch.craft.executeScraper
import watch.craft.noDesc
import java.net.URI

class HowlingHopsScraperTest {
  companion object {
    private val ITEMS = executeScraper(HowlingHopsScraper())
  }

  @Test
  fun `finds all the beers`() {
    assertEquals(14, ITEMS.size)
  }

  @Test
  fun `extracts sale price not original price`() {
    assertEquals(
      ScrapedItem(
        name = "Push Push",
        summary = "DDH Pale Ale",
        offers = setOf(
          Offer(quantity = 4, totalPrice = 16.00, sizeMl = 440)
        ),
        abv = 5.8,
        available = true,
        thumbnailUrl = URI("https://www.howlinghops.co.uk/wp-content/uploads/2020/06/push-push-440ml-324x324.png")
      ),
      ITEMS.byName("Push Push").noDesc()
    )
  }

  @Test
  fun `identifies mixed cases`() {
    assertTrue(ITEMS.byName("NEW 12 Beer Mega Pack").mixed)
  }

  @Test
  @Disabled
  fun `identifies out-of-stock items`() {
    assertFalse(ITEMS.byName("Buckle Down").available)
  }

  @Test
  fun `doesn't extract apparel`() {
    assertTrue(ITEMS.none { it.name.contains("various colours") })
  }

  @Test
  fun `extracts description`() {
    assertNotNull(ITEMS.byName("Push Push").desc)
  }
}

