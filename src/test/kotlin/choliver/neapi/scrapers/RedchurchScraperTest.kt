package choliver.neapi.scrapers

import choliver.neapi.Scraper
import choliver.neapi.byName
import choliver.neapi.executeScraper
import choliver.neapi.noDesc
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.net.URI

class RedchurchScraperTest {
  companion object {
    private val ITEMS = executeScraper(RedchurchScraper())
  }

  @Test
  fun `finds all the beers`() {
    assertEquals(17, ITEMS.size)
  }

  @Test
  fun `extracts beer details`() {
    assertEquals(
      Scraper.Item(
        name = "Shoreditch Blonde",
        sizeMl = 330,
        abv = 4.5,
        perItemPrice = 1.46,    // Best price
        available = true,
        thumbnailUrl = URI("https://cdn.shopify.com/s/files/1/0034/8694/1229/products/SBwwwimage_200x200.png")
      ),
      ITEMS.byName("Shoreditch Blonde").noDesc()
    )
  }

  @Test
  fun `identifies mixed cases`() {
    val mixedCases = ITEMS.filter { it.mixed }

    assertFalse(mixedCases.isEmpty())
    assertFalse(mixedCases.any { it.name.contains("Mixed case") })
  }

  @Test
  fun `identifies sold out`() {
    assertFalse(ITEMS.byName("Hoxton Stout").available)
  }

  @Test
  fun `ignores things that aren't beers`() {
    assertEquals(
      emptyList<String>(),
      ITEMS.map { it.name }.filter { it.contains("glass", ignoreCase = true) }
    )
  }

  @Test
  fun `extracts description`() {
    assertNotNull(ITEMS.byName("Shoreditch Blonde").desc)
  }
}

