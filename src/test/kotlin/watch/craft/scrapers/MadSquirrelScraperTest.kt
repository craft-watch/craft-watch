package watch.craft.scrapers

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import watch.craft.Offer
import watch.craft.Scraper.Node.ScrapedItem
import watch.craft.byName
import watch.craft.dsl.containsMatch
import watch.craft.executeScraper
import watch.craft.noDesc
import java.net.URI

class MadSquirrelScraperTest {
  companion object {
    private val ITEMS = executeScraper(MadSquirrelScraper(), dateString = "2020-08-02")
  }

  @Test
  fun `finds all the beers`() {
    assertEquals(15, ITEMS.size)
  }

  @Test
  fun `extracts beer details`() {
    assertEquals(
      ScrapedItem(
        name = "Evolve",
        summary = "Contemporary Red",
        offers = setOf(
          Offer(quantity = 1, totalPrice = 3.60, sizeMl = 440)
        ),
        available = true,
        thumbnailUrl = URI("https://www.madsquirrelbrew.co.uk/uploads/images/products/thumbs/mad-squirrel-evolve-1593437621Evolve-Can-Mockup.png")
      ),
      ITEMS.byName("Evolve").noDesc()
    )
  }

  @Test
  fun `extracts description`() {
    assertNotNull(ITEMS.byName("Evolve").desc)
  }

  @Test
  fun `cleans up names`() {
    assertFalse(ITEMS.any { it.name.containsMatch("\\d+ml") })
  }
}

