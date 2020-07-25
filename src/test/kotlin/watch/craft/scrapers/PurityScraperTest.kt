package watch.craft.scrapers

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import watch.craft.*
import watch.craft.Format.CAN
import watch.craft.Format.KEG
import watch.craft.Scraper.Node.ScrapedItem
import java.net.URI

class PurityScraperTest {
  companion object {
    private val ITEMS = executeScraper(PurityScraper(), dateString = "2020-07-16")
  }

  @Test
  fun `finds all the beers`() {
    assertEquals(23, ITEMS.size)
  }

  @Test
  fun `extracts beer details`() {
    assertEquals(
      ScrapedItem(
        name = "India Red Ale",
        abv = 7.4,
        offers = setOf(
          Offer(totalPrice = 3.70, quantity = 1, sizeMl = 440, format = CAN),
          Offer(totalPrice = 40.00, quantity = 12, sizeMl = 440, format = CAN)
        ),
        available = true,
        thumbnailUrl = URI("https://khcdn58fb495229.b-cdn.net/wp-content/uploads/India-Red-Ale-300x300.png")
      ),
      ITEMS.byName("India Red Ale").noDesc()
    )
  }

  @Test
  fun `extracts description`() {
    assertNotNull(ITEMS.byName("India Red Ale").desc)
  }

  @Test
  fun `removes nonsense from names`() {
    assertFalse(ITEMS.any { it.name.contains("^Purity".toRegex()) })
    assertFalse(ITEMS.any { it.name.contains("mini cask", ignoreCase = true) })
    assertFalse(ITEMS.any { it.name.contains("polypin", ignoreCase = true) })
  }

  @Test
  fun `identifies polypins`() {
    val items = ITEMS.filter { it.offers.any { offer -> offer.sizeMl == 20_000 } }

    assertFalse(items.isEmpty())
    assertFalse(items.any { it.onlyOffer().format != KEG })
  }

  @Test
  fun `identifies casks`() {
    val items = ITEMS.filter { it.offers.any { offer -> offer.sizeMl == 5_000 } }

    assertFalse(items.isEmpty())
    assertFalse(items.any { it.onlyOffer().format != KEG })
  }

  @Test
  fun `ignores pub mixed packs`() {
    assertFalse(ITEMS.any { it.mixed })
  }

  @Test
  fun `selects sale price`() {
    val item = ITEMS.first { it.name == "Bunny Hop" && it.offers.none { offer -> offer.format == KEG } }

    assertEquals(
      listOf(1.95, 41.20),   // The latter is the sale price
      item.offers.map { it.totalPrice }
    )
  }

  @Test
  fun `doesn't select crazy ABV`() {
    assertEquals(4.6, ITEMS.byName("Pure Cider").abv)   // Not "100%" which also appears in the text
  }
}
