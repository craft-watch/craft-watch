package choliver.neapi.scrapers

import choliver.neapi.CACHE_DIR
import choliver.neapi.HttpGetter
import choliver.neapi.ParsedItem
import choliver.neapi.RealScraperContext
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.net.URI

class VillagesScraperTest {
  private val getter = HttpGetter(CACHE_DIR)
  private val ctx = RealScraperContext(getter)
  private val items = with(VillagesScraper()) { ctx.scrape() }

  @Test
  fun `finds all the beers`() {
    assertEquals(7, items.size)
  }

  @Test
  fun `extracts beer details`() {
    assertTrue(
      ParsedItem(
        name = "RODEO Pale Ale",
        abv = "4.6".toBigDecimal(),
        price = "25.60".toBigDecimal(),
        available = true,
        thumbnailUrl = URI("https://cdn.shopify.com/s/files/1/0360/4735/5948/products/VILLAGES_RODEO_PALE_ALE_330ML_CAN_345x345.jpg"),
        url = URI("https://villagesbrewery.com/collections/buy-beer/products/rodeo-pale-ale")
      ) in items
    )
  }

  @Test
  fun `extracts case details`() {
    assertTrue(
      ParsedItem(
        name = "VILLAGES Mixed Case (24 × cans)",
        abv = null,   // Can't find this!
        price = "52.50".toBigDecimal(),
        available = true,
        thumbnailUrl = URI("https://cdn.shopify.com/s/files/1/0360/4735/5948/products/VILLAGES_MIXED_CASE_345x345.jpg"),
        url = URI("https://villagesbrewery.com/collections/buy-beer/products/villages-mixed-case")
      ) in items
    )
  }
}

