package watch.craft.scrapers

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import watch.craft.Item
import watch.craft.byName
import watch.craft.executeScraper
import watch.craft.noDesc

class PillarsScraperTest {
  companion object {
    private val ITEMS = executeScraper(PillarsScraper())
  }

  @Test
  fun `finds all the beers`() {
    assertEquals(6, ITEMS.size)
  }

  @Test
  fun `extracts beer details`() {
    assertEquals(
      Item(
        brewery = "Pillars",
        name = "Pillars Icebock",
        summary = "Eisbock",
        sizeMl = null,
        abv = 8.0,
        perItemPrice = 6.00,
        available = true,
        url = "https://shop.pillarsbrewery.com/collections/pillars-beers/products/pillars-icebock",
        thumbnailUrl = "https://cdn.shopify.com/s/files/1/0367/7857/3883/products/Icebock_Shopify_1edf8964-413d-4ad8-9b05-9a9672a48796_250x250.png"
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
    val item = ITEMS.byName("Untraditional Lager") // Note "case" no longer in title
    assertEquals(1.88, item.perItemPrice)   // Divided price
  }

  @Test
  fun `ignores things that aren't beers`() {
    assertEquals(
      emptyList<String>(),
      ITEMS.map { it.name }.filter { it.contains("gift card", ignoreCase = true) }
    )
  }

  @Test
  fun `sanitises description`() {
    assertFalse(ITEMS.byName("Pillars Icebock").desc!!.contains("STYLE"))
  }
}

