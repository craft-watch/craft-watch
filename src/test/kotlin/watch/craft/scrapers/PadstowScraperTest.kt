package watch.craft.scrapers

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import watch.craft.Offer
import watch.craft.Scraper.ScrapedItem
import watch.craft.byName
import watch.craft.executeScraper
import watch.craft.noDesc
import java.net.URI

class PadstowScraperTest {
  companion object {
    private val ITEMS = executeScraper(PadstowScraper())
  }

  @Test
  fun `finds all the beers`() {
    assertEquals(35, ITEMS.size)
  }

  @Test
  fun `extracts beer details`() {
    assertEquals(
      ScrapedItem(
        name = "Pocket Rocket",
        summary = "Extra hoppy Cornish Pale Ale",
        sizeMl = 440,
        abv = 4.0,
        offers = setOf(Offer(totalPrice = 3.95)),
        available = true,
        thumbnailUrl = URI("https://www.padstowbrewing.co.uk/wp-content/uploads/2019/10/Pocket-Rocket-Can-1.jpg")
      ),
      ITEMS.first { it.name == "Pocket Rocket" && !it.keg }.noDesc()
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

