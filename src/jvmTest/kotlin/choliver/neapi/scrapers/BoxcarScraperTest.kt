package choliver.neapi.scrapers

import choliver.neapi.ParsedItem
import choliver.neapi.executeScraper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.net.URI

class BoxcarScraperTest {
  private val items = executeScraper(BoxcarScraper())

  @Test
  fun `finds all the beers`() {
    assertEquals(8, items.size)
  }

  @Test
  fun `extracts available beers`() {
    assertTrue(
      ParsedItem(
        name = "Dreamful",
        abv = "6.5".toBigDecimal(),
        price = "4.95".toBigDecimal(),
        available = true,
        thumbnailUrl = URI("https://cdn.shopify.com/s/files/1/0358/6742/6953/products/IMG-20200604-WA0003_345x345.jpg"),
        url = URI("https://shop.boxcarbrewery.co.uk/collections/beer/products/dreamful-6-5-ipa-440ml")
      ) in items
    )
  }

  @Test
  fun `extracts unavailable beers`() {
    assertTrue(
      ParsedItem(
        name = "Dark Mild",
        abv = "3.6".toBigDecimal(),
        price = "3.75".toBigDecimal(),
        available = false,
        thumbnailUrl = URI("https://cdn.shopify.com/s/files/1/0358/6742/6953/products/20200429_183043_345x345.jpg"),
        url = URI("https://shop.boxcarbrewery.co.uk/collections/beer/products/dark-mild")
      ) in items
    )
  }
}

