package watch.craft.scrapers

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import watch.craft.Scraper.ScrapedItem
import watch.craft.byName
import watch.craft.executeScraper
import watch.craft.noDesc
import java.net.URI

class PillarsScraperTest {
  companion object {
    private val ITEMS = executeScraper(PillarsScraper())
  }

  @Test
  fun `finds all the beers`() {
    assertEquals(8, ITEMS.size)
  }

  @Test
  fun `extracts beer details`() {
    assertEquals(
      ScrapedItem(
        name = "Pillars Icebock",
        summary = "Eisbock",
        sizeMl = null,
        abv = 8.0,
        totalPrice = 6.00,
        available = true,
        thumbnailUrl = URI("https://cdn.shopify.com/s/files/1/0367/7857/3883/products/Icebock_Shopify_1edf8964-413d-4ad8-9b05-9a9672a48796_250x250.png?v=1591904521")
      ),
      ITEMS.byName("Pillars Icebock").noDesc()
    )
  }

  @Test
  fun `identifies kegs`() {
    val item = ITEMS.byName("Pillars Pilsner") // Note "keg" no longer in title
    assertEquals(5000, item.sizeMl)
    assertTrue(item.keg)
  }

  @Test
  fun `identifies cases`() {
    ITEMS.byName("Untraditional Lager") // Note "case" no longer in title
  }

  @Test
  fun `ignores things that aren't beers`() {
    assertEquals(
      emptyList<String>(),
      ITEMS.map { it.name }.filter { it.contains("gift card", ignoreCase = true) }
    )
  }

  @Test
  fun `extracts description`() {
    assertNotNull(ITEMS.byName("Pillars Icebock").desc)
  }
}

