package watch.craft.scrapers

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import watch.craft.*
import watch.craft.Format.KEG
import watch.craft.Scraper.ScrapedItem
import java.net.URI

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
      ScrapedItem(
        name = "Rebell Helles",
        summary = "Helles Lager",
        abv = 4.8,
        offers = setOf(Offer(quantity = 24, totalPrice = 45.00)),
        available = true,
        thumbnailUrl = URI("https://cdn.shopify.com/s/files/1/0367/7857/3883/products/01125465-bf55-4019-9d59-52d2d00d1333_250x250.png?v=1585771178")
      ),
      ITEMS.first { it.name == "Rebell Helles" && it.onlyOffer().quantity == 24 }.noDesc()
    )
  }

  @Test
  fun `identifies kegs`() {
    val item = ITEMS.byName("Pillars Tropical Pilsner") // Note "keg" no longer in title
    assertEquals(5000, item.onlyOffer().sizeMl)
    assertEquals(KEG, item.onlyOffer().format)
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
    assertNotNull(ITEMS.byName("Rebell Helles").desc)
  }
}

