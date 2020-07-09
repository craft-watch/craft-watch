package watch.craft.scrapers

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import watch.craft.Scraper.ScrapedItem
import watch.craft.byName
import watch.craft.executeScraper
import watch.craft.noDesc
import java.net.URI

class SolvayScraperTest {
  companion object {
    private val ITEMS = executeScraper(SolvayScraper(), dateString = "2020-07-07")
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
        sizeMl = 750,
        totalPrice = 12.80,
        available = true,
        thumbnailUrl = URI("https://static1.squarespace.com/static/5e3fd955244c110e4deb8fff/5eec7c5c91751c60400a0ed2/5eec941ff809127782259a36/1592567567620/")
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
    assertEquals(6, ITEMS.byName("8:20").quantity)
  }

  @Test
  fun `identifies mixed`() {
    assertFalse(ITEMS.none { it.mixed })
  }

  @Test
  fun `identifies kegs`() {
    val items = ITEMS.filter { it.keg }

    assertFalse(items.isEmpty())
    assertTrue(items.all { it.sizeMl!! >= 1000 })
  }
}

