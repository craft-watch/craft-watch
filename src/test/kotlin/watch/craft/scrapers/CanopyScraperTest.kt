package watch.craft.scrapers

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import watch.craft.Offer
import watch.craft.Scraper.ScrapedItem
import watch.craft.byName
import watch.craft.executeScraper
import watch.craft.noDesc
import java.net.URI
import kotlin.text.RegexOption.IGNORE_CASE

class CanopyScraperTest {
  companion object {
    private val ITEMS = executeScraper(CanopyScraper())
  }

  @Test
  fun `finds all the beers`() {
    assertEquals(8, ITEMS.size)
  }

  @Test
  fun `extracts beer details`() {
    assertEquals(
      ScrapedItem(
        name = "Brockwell IPA",   // ABV removed from name
        offers = setOf(Offer(totalPrice = 2.50)),
        abv = 5.6,
        sizeMl = 330,
        available = true,
        thumbnailUrl = URI("https://cdn.shopify.com/s/files/1/0060/1574/6161/products/CB-Assets-Can-Master-640x625-330ml-Brockwell-F_large.png?v=1539104364")
      ),
      ITEMS.byName("Brockwell IPA").noDesc()
    )
  }

  @Test
  fun `identifies sold out`() {
    assertFalse(ITEMS.byName("Sunray Pale Ale").available)
  }

  @Test
  fun `excludes packs`() {
    assertTrue(ITEMS.none { it.name.contains("box|pack".toRegex(IGNORE_CASE)) })
  }

  @Test
  fun `extracts description`() {
    assertNotNull(ITEMS.byName("Brockwell IPA").desc)
  }
}

