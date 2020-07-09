package watch.craft.scrapers

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import watch.craft.*
import watch.craft.Scraper.ScrapedItem
import java.net.URI
import kotlin.text.RegexOption.IGNORE_CASE

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
        name = "Great Northern Lager",
        abv = 4.3,
        offers = setOf(
          Offer(totalPrice = 3.00, sizeMl = 440)
        ),
        available = true,
        thumbnailUrl = URI("https://cdn.shopify.com/s/files/1/2213/3151/products/2020-NMBC_Great-Northern-Lager-29_180x.jpg?v=1589212703")
      ),
      ITEMS.first { it.name == "Great Northern Lager" && it.onlyOffer().quantity == 1 }.noDesc()
    )
  }

  @Test
  fun `extracts description`() {
    assertNotNull(ITEMS.byName("Retro Faith").desc)
  }

  @Test
  fun `extracts summaries from various places`() {
    assertEquals("Gluten Free IPA", ITEMS.byName("Origin").summary)   // Extracted from after "TM"
    assertEquals("Hazy Pale Ale", ITEMS.byName("Retro Faith").summary)  // Extracted from after "//"
  }

  @Test
  fun `identifies multi-packs`() {
    assertEquals(12, ITEMS.byName("Keep The Faith").onlyOffer().quantity)
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
    assertFalse(ITEMS.any { it.name.contains("//|pack|™".toRegex(IGNORE_CASE)) })
  }
}

