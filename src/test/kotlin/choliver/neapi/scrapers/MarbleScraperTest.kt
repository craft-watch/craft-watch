package choliver.neapi.scrapers

import choliver.neapi.Scraper
import choliver.neapi.executeScraper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import java.net.URI

class MarbleScraperTest {
  companion object {
    private val ITEMS = executeScraper(MarbleScraper())
  }

  @Test
  fun `finds all the beers`() {
    assertEquals(28, ITEMS.size)
  }

  @Test
  fun `extracts beer details`() {
    assertEquals(
      Scraper.Item(
        name = "Earl Grey IPA",
        summary = "IPA",
        sizeMl = 500,
        abv = 6.8,
        perItemPrice = 4.00,
        available = true,
        thumbnailUrl = URI("https://marblebeers.com/wp-content/uploads/2017/11/Earl-Grey-500ml-Can-234x300.png")
      ),
      ITEMS.first { it.name == "Earl Grey IPA" }
    )
  }

  @Test
  fun `identifies mixed cases`() {
    val cases = ITEMS.filter { it.mixed }

    assertFalse(cases.isEmpty())
    assertFalse(cases.any { it.summary != null })
  }

  @Test
  fun `identifies kegs`() {
    val kegs = ITEMS.filter { it.keg }

    assertFalse(kegs.isEmpty())
    assertFalse(kegs.any { it.name.contains("mini", ignoreCase = true) })
  }

  @Test
  fun `identifies sold out`() {
    assertFalse(ITEMS.first { it.name == "Cross Collar Half" }.available)
  }

  @Test
  fun `removes irritating suffixes`() {
    assertFalse(ITEMS.any { it.name.contains("cans", ignoreCase = true) })
    assertFalse(ITEMS.any { it.name.contains("\\d+$".toRegex()) })
  }
}

