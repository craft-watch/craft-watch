package choliver.neapi.scrapers

import choliver.neapi.Scraper.Item
import choliver.neapi.executeScraper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.net.URI

class PillarsScraperTest {
  companion object {
    private val ITEMS = executeScraper(PillarsScraper())
  }

  @Test
  fun `finds all the beers`() {
    assertEquals(7, ITEMS.size)
  }

  @Test
  fun `extracts beer details`() {
    assertEquals(
      Item(
        name = "Pillars Icebock",
        summary = "Eisbock",
        sizeMl = null,
        abv = 8.0,
        perItemPrice = 6.00,
        available = true,
        thumbnailUrl = URI("https://cdn.shopify.com/s/files/1/0367/7857/3883/products/Icebock_Shopify_1edf8964-413d-4ad8-9b05-9a9672a48796_250x250.png")
      ),
      ITEMS.first { it.name == "Pillars Icebock" }
    )
  }

  @Test
  fun `identifies kegs`() {
    val item = ITEMS.first { it.name == "Pillars Pilsner" } // Note "keg" no longer in title
    assertEquals(5000, item.sizeMl)
    assertEquals("Minikeg", item.summary)
  }

  @Test
  fun `identifies cases`() {
    val item = ITEMS.first { it.name == "Untraditional Lager" } // Note "case" no longer in title
    assertEquals(1.88, item.perItemPrice)   // Divided price
  }

  @Test
  fun `ignores things that aren't beers`() {
    assertEquals(
      emptyList<String>(),
      ITEMS.map { it.name }.filter { it.contains("gift card", ignoreCase = true) }
    )
  }
}

