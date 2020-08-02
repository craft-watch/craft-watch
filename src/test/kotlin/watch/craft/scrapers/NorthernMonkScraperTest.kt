package watch.craft.scrapers

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import watch.craft.*
import watch.craft.Format.CAN
import watch.craft.Scraper.Node.ScrapedItem
import java.net.URI
import kotlin.text.RegexOption.IGNORE_CASE

class NorthernMonkScraperTest {
  companion object {
    private val ITEMS = executeScraper(NorthernMonkScraper(), dateString = "2020-08-02")
  }

  @Test
  fun `finds all the beers`() {
    assertEquals(29, ITEMS.size)
  }

  @Test
  fun `extracts beer details`() {
    assertEquals(
      ScrapedItem(
        name = "Faith",
        summary = "Hazy Pale Ale",
        abv = 5.4,
        offers = setOf(
          Offer(totalPrice = 3.10, sizeMl = 440, format = CAN)
        ),
        available = true,
        thumbnailUrl = URI("https://cdn.shopify.com/s/files/1/2213/3151/products/FAITH_2020_200x.jpg")
      ),
      ITEMS.first { it.name == "Faith" && it.onlyOffer().quantity == 1 }.noDesc()
    )
  }

  @Test
  fun `extracts description`() {
    assertNotNull(ITEMS.byName("Faith").desc)
  }

  @Test
  fun `extracts summaries from various places`() {
    assertEquals("Gluten Free IPA", ITEMS.byName("Origin").summary)   // Extracted from after "TM"
    assertEquals("Hazy Pale Ale", ITEMS.byName("Faith").summary)  // Extracted from after "//"
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
  fun `identifies sold-out`() {
    assertFalse(ITEMS.byName("Heathen").available)
  }

  @Test
  fun `ignores things that aren't beers`() {
    assertFalse(ITEMS.any { it.name.contains("gift", ignoreCase = true) })
  }

  @Test
  fun `no nonsense in names`() {
    assertFalse(ITEMS.any { it.name.contains("//|pack|™".toRegex(IGNORE_CASE)) })
  }

  @Test
  fun `identifies quantities for non-standard pattern`() {
    assertEquals(24, ITEMS.byName("Striding Edge Hazy Light IPA").onlyOffer().quantity)
  }
}

