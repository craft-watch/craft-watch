package watch.craft.scrapers

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import watch.craft.*
import watch.craft.Format.KEG
import watch.craft.Scraper.ScrapedItem
import java.net.URI

class MarbleScraperTest {
  companion object {
    private val ITEMS = executeScraper(MarbleScraper())
  }

  @Test
  fun `finds all the beers`() {
    assertEquals(24, ITEMS.size)
  }

  @Test
  fun `extracts beer details`() {
    assertEquals(
      ScrapedItem(
        name = "Earl Grey IPA",
        summary = "IPA",
        abv = 6.8,
        offers = setOf(
          Offer(totalPrice = 4.00, sizeMl = 500)
        ),
        available = true,
        thumbnailUrl = URI("https://marblebeers.com/wp-content/uploads/2017/11/Earl-Grey-500ml-Can-234x300.png")
      ),
      ITEMS.byName("Earl Grey IPA").noDesc()
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
    val kegs = ITEMS.filter { it.onlyOffer().format == KEG }

    assertFalse(kegs.isEmpty())
    assertFalse(kegs.any { it.name.contains("mini", ignoreCase = true) })
  }

  @Test
  fun `identifies sold out`() {
    assertFalse(ITEMS.byName("Cross Collar").available)
  }

  @Test
  fun `removes irritating suffixes`() {
    assertFalse(ITEMS.any { it.name.contains("cans", ignoreCase = true) })
    assertFalse(ITEMS.any { it.name.contains("\\d+$".toRegex()) })
  }

  @Test
  fun `extracts description`() {
    assertNotNull(ITEMS.byName("Earl Grey IPA").desc)
  }
}

