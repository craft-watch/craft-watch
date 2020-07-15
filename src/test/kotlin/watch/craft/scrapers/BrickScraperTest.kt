package watch.craft.scrapers

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import watch.craft.*
import watch.craft.Format.CAN
import watch.craft.Scraper.ScrapedItem
import java.net.URI
import kotlin.text.RegexOption.IGNORE_CASE

class BrickScraperTest {
  companion object {
    private val ITEMS = executeScraper(BrickScraper(), dateString = null)
  }

  @Test
  fun `finds all the beers`() {
    ITEMS.display()
    assertEquals(17, ITEMS.size)
  }

  @Test
  fun `extracts beer details`() {
    assertEquals(
      ScrapedItem(
        name = "Spring NEDIPA",
        summary = "New England Double IPA",
        abv = 8.0,
        offers = setOf(
          Offer(totalPrice = 5.85, quantity = 1, sizeMl = 440, format = CAN)
        ),
        available = true,
        thumbnailUrl = URI("https://cdn.shopify.com/s/files/1/0360/1707/8412/products/BB_Tall_can_mockup_NEDIPA_right_label_v5_1_250x250.jpg?v=1591365463")
      ),
      ITEMS.byName("Spring NEDIPA").noDesc()
    )
  }

  @Test
  fun `extracts description`() {
    assertNotNull(ITEMS.byName("Spring NEDIPA").desc)
  }

  @Test
  fun `identifies mixed`() {
    val items = ITEMS.filter { it.mixed }

    assertFalse(items.isEmpty())
    assertFalse(items.any { it.onlyOffer().quantity == 1 })
  }

  @Test
  fun `cleans up multi-pack names`() {
    val item = ITEMS.first { it.name == "Peckham Pale" && it.onlyOffer().quantity > 1 }

    assertFalse(item.name.contains("case", ignoreCase = true))
  }

  @Test
  fun `ignores non-beers`() {
    assertFalse(ITEMS.any { it.name.contains("donate|stubbby".toRegex(IGNORE_CASE)) })
  }
}

