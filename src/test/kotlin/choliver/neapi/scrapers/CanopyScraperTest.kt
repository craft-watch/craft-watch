package choliver.neapi.scrapers

import choliver.neapi.ScrapedItem
import choliver.neapi.executeScraper
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.net.URI
import kotlin.text.RegexOption.IGNORE_CASE

class CanopyScraperTest {
  companion object {
    private val ITEMS = executeScraper(CanopyScraper())
  }

  @Test
  fun `finds all the beers`() {
    assertEquals(7, ITEMS.size)
  }

  @Test
  fun `extracts beer details`() {
    assertEquals(
      ScrapedItem(
        name = "Brockwell IPA",   // ABV removed from name
        perItemPrice = 2.50,
        abv = 5.6,
        sizeMl = 330,
        available = true,
        thumbnailUrl = URI("https://cdn.shopify.com/s/files/1/0060/1574/6161/products/CB-Assets-Can-Master-640x625-330ml-Brockwell-F_large.png?v=1539104364"),
        url = URI("https://shop.canopybeer.com/products/brockwell-ipa")
      ),
      ITEMS.first { it.name == "Brockwell IPA" }
    )
  }

  @Test
  fun `identifies sold out`() {
    assertFalse(ITEMS.first { it.name == "Champion Kölsch" }.available)
  }

  @Test
  fun `excludes packs`() {
    assertTrue(ITEMS.none { it.name.contains("box|pack".toRegex(IGNORE_CASE)) })
  }


}

