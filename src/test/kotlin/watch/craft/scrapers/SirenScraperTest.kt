package watch.craft.scrapers

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import watch.craft.Scraper.ScrapedItem
import watch.craft.byName
import watch.craft.executeScraper
import watch.craft.noDesc
import java.net.URI

class SirenScraperTest {
  companion object {
    private val ITEMS = executeScraper(SirenScraper(), dateString = "2020-07-04")
  }

  @Test
  fun `finds all the beers`() {
    ITEMS.forEach(::println)
    assertEquals(21, ITEMS.size)
  }

  @Test
  fun `extracts beer details`() {
    assertEquals(
      ScrapedItem(
        name = "Heart of Ice",
        summary = "Modern Lager",
        abv = 4.5,
        sizeMl = 440,
        price = 3.00,
        available = true,
        thumbnailUrl = URI("https://www.sirencraftbrew.com/uploads/images/products/large/siren-craft-brew-siren-craft-brew-heart-of-ice-1593593312siren-craft-brew-heart-of-ice-440.png")
      ),
      ITEMS.byName("Heart of Ice").noDesc()
    )
  }

  @Test
  fun `extracts description`() {
    assertNotNull(ITEMS.byName("Heart of Ice").desc)
  }

  @Test
  fun `identifies kegs`() {
    val kegs = ITEMS.filter { it.keg }

    assertFalse(kegs.isEmpty())
    assertTrue(kegs.all { it.sizeMl!! >= 1000 })
    assertTrue(kegs.none { it.name.contains("keg", ignoreCase = true) })
  }
}

