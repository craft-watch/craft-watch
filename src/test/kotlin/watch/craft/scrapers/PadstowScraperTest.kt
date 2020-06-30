package watch.craft.scrapers

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import watch.craft.Item
import watch.craft.byName
import watch.craft.executeScraper
import watch.craft.noDesc

class PadstowScraperTest {
  companion object {
    private val ITEMS = executeScraper(PadstowScraper(), dateString = null)
  }

  @Test
  fun `finds all the beers`() {
    assertEquals(32, ITEMS.size)
  }

  @Test
  fun `extracts beer details`() {
    assertEquals(
      Item(
        brewery = "Padstow",
        name = "Pocket Rocket",
        summary = "Extra hoppy Cornish Pale Ale",
        sizeMl = 440,
        abv = 4.0,
        perItemPrice = 3.95,
        available = true,
        url = "https://www.padstowbrewing.co.uk/product/pocket-rocket/",
        thumbnailUrl = "https://www.padstowbrewing.co.uk/wp-content/uploads/2019/10/Pocket-Rocket-Can-1.jpg"
      ),
      ITEMS.byName("Pocket Rocket").noDesc()
    )
  }

  @Test
  fun `extracts description`() {
    assertNotNull(ITEMS.byName("Pocket Rocket").desc)
  }

  @Test
  fun `identifies mixed cases`() {
    val mixedCases = ITEMS.filter { it.mixed }

    assertFalse(mixedCases.isEmpty())
  }

  @Test
  fun `identifies kegs`() {
    val kegs = ITEMS.filter { it.keg }

    assertFalse(kegs.isEmpty())
    assertTrue(kegs.all { it.sizeMl!! >= 1000 })
  }

  @Test
  fun `removes noise from names`() {
    assertFalse(ITEMS.any { it.name.contains("keg", ignoreCase = true) })
    assertFalse(ITEMS.any { it.name.contains("-pack", ignoreCase = true) })
    assertFalse(ITEMS.any { it.name.contains("-", ignoreCase = true) })
  }
}

