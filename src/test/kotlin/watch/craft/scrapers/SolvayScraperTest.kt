package watch.craft.scrapers

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import watch.craft.*
import watch.craft.Format.KEG
import watch.craft.Scraper.Node.ScrapedItem
import java.net.URI

class SolvayScraperTest {
  companion object {
    private val ITEMS = executeScraper(SolvayScraper())
  }

  @Test
  fun `finds all the beers`() {
    assertEquals(8, ITEMS.size)
  }

  @Test
  fun `extracts beer details`() {
    assertEquals(
      ScrapedItem(
        name = "Charm",
        summary = "Barrel-Aged Amber Ale",
        abv = 6.3,
        offers = setOf(
          Offer(totalPrice = 12.80, sizeMl = 750)
        ),
        available = true,
        thumbnailUrl = URI("https://images.squarespace-cdn.com/content/v1/5e3fd955244c110e4deb8fff/1592567554650-Q1SD6HPJAHHLJCBYNHOB/ke17ZwdGBToddI8pDm48kLXCf88_9uNTKXkq27cF4sB7gQa3H78H3Y0txjaiv_0fDoOvxcdMmMKkDsyUqMSsMWxHk725yiiHCCLfrh8O1z5QHyNOqBUUEtDDsRWrJLTmwbA6upbL5Bu97tJociXJklKprRMdH2Tl4F1PjaoPT3YUs5wkl5ojCV1O900UJ7ME/charm.png?format=200w")
      ),
      ITEMS.byName("Charm").noDesc()
    )
  }

  @Test
  fun `extracts description`() {
    assertNotNull(ITEMS.byName("Charm").desc)
  }

  @Test
  fun `identifies multi-packs`() {
    assertEquals(6, ITEMS.byName("8:20").onlyOffer().quantity)
  }

  @Test
  fun `identifies mixed`() {
    assertFalse(ITEMS.none { it.mixed })
  }

  @Test
  fun `identifies kegs`() {
    val items = ITEMS.filter { it.onlyOffer().format == KEG }

    assertFalse(items.isEmpty())
    assertTrue(items.all { it.onlyOffer().sizeMl!! >= 1000 })
  }
}

