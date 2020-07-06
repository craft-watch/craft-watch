package watch.craft.scrapers

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import watch.craft.Scraper.ScrapedItem
import watch.craft.byName
import watch.craft.executeScraper
import watch.craft.noDesc
import java.net.URI

class NorthernMonkScraperTest {
  companion object {
    private val ITEMS = executeScraper(NorthernMonkScraper(), dateString = "2020-07-06")
  }

  @Test
  fun `finds all the beers`() {
    assertEquals(15, ITEMS.size)
  }

  @Test
  fun `extracts beer details`() {
    assertEquals(
      ScrapedItem(
        name = "Retro Faith",
        summary = "Hazy Pale Ale",
        abv = 5.4,
        sizeMl = 440,
        price = 3.10,
        available = true,
        thumbnailUrl = URI("https://cdn.shopify.com/s/files/1/2213/3151/products/RETRO_FAITH_440_180x.jpg?v=1592465054")
      ),
      ITEMS.byName("Retro Faith").noDesc()
    )
  }

  @Test
  fun `extracts description`() {
    assertNotNull(ITEMS.byName("Retro Faith").desc)
  }

  @Test
  fun `identifies multi-packs`() {
    assertEquals(12, ITEMS.byName("Keep The Faith").numItems)
  }

  @Test
  fun `identifies mixed cases`() {
    assertTrue(ITEMS.byName("Enter The Haze").mixed)
  }

  @Test
  fun `ignores things that aren't beers`() {
    assertFalse(ITEMS.any { it.name.contains("gift", ignoreCase = true) })
  }

  @Test
  fun `no nonsense in names`() {
    assertFalse(ITEMS.any { it.name.contains("//") || it.name.contains("pack", ignoreCase = true) })
  }
}

