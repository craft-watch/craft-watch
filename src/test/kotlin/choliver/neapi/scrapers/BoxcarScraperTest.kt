package choliver.neapi.scrapers

import choliver.neapi.Scraper.Result.Item
import choliver.neapi.executeScraper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.net.URI

class BoxcarScraperTest {
  companion object {
    private val ITEMS = executeScraper(BoxcarScraper())
  }

  @Test
  fun `finds all the beers`() {
    assertEquals(8, ITEMS.size)
  }

  @Test
  fun `extracts available beers`() {
    assertEquals(
      Item(
        name = "Dreamful",
        summary = "IPA",
        sizeMl = 440,
        abv = 6.5,
        perItemPrice = 4.95,
        available = true,
        thumbnailUrl = URI("https://cdn.shopify.com/s/files/1/0358/6742/6953/products/IMG-20200604-WA0003_345x345.jpg")
      ),
      ITEMS.first { it.name == "Dreamful" }
    )
  }

  @Test
  fun `extracts unavailable beers`() {
    assertEquals(
      Item(
        name = "Dark Mild",
        sizeMl = 440,
        abv = 3.6,
        perItemPrice = 3.75,
        available = false,
        thumbnailUrl = URI("https://cdn.shopify.com/s/files/1/0358/6742/6953/products/20200429_183043_345x345.jpg")
      ),
      ITEMS.first { it.name == "Dark Mild" }
    )
  }
}

