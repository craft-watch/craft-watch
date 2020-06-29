package watch.craft.scrapers

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import watch.craft.Item
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
    assertEquals(12, ITEMS.size)
  }

  @Test
  fun `extracts sale price not original price`() {
    assertEquals(
      Item(
        brewery = "Howling Hops",
        name = "Push Push",
        summary = "DDH Pale Ale",
        perItemPrice = 4.00,
        abv = 5.8,
        sizeMl = 440,
        available = true,
        url = "https://www.howlinghops.co.uk/product/push-push-4-x-440ml/",
        thumbnailUrl = "https://www.howlinghops.co.uk/wp-content/uploads/2020/06/push-push-440ml-324x324.png"
      ),
      ITEMS.byName("Push Push").noDesc()
    )
  }

  @Test
  fun `identifies mixed cases`() {
    assertTrue(ITEMS.byName("NEW 12 Beer Mega Pack").mixed)
  }

  @Test
  fun `identifies out-of-stock items`() {
    assertFalse(ITEMS.byName("Off Ramp").available
    )
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

