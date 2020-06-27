package watch.craft.scrapers

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import watch.craft.Scraper.Item
import watch.craft.byName
import watch.craft.executeScraper
import watch.craft.noDesc
import java.net.URI
import kotlin.text.RegexOption.IGNORE_CASE

class FourpureScraperTest {
  companion object {
    private val ITEMS = executeScraper(FourpureScraper())
  }

  @Test
  fun `finds all the beers`() {
    assertEquals(10, ITEMS.size)
  }

  @Test
  fun `excludes packs`() {
    assertTrue(ITEMS.none { it.name.contains("pack".toRegex(IGNORE_CASE)) })
  }

  @Test
  fun `strips size from non-keg name`() {
    assertTrue(ITEMS.none { it.name.contains("ml", ignoreCase = true) })
  }

  @Test
  fun `extracts non-keg details`() {
    assertEquals(
      Item(
        name = "Basecamp",
        perItemPrice = 2.00,
        abv = 4.7,
        sizeMl = 330,
        available = true,
        thumbnailUrl = URI("https://www.fourpure.com/uploads/images/products/thumbs/fourpurebrewingco._fourpure_pilslager_1566986321BASECAMPNB.png")
      ),
      ITEMS.byName("Basecamp").noDesc()
    )
  }

  @Test
  fun `extracts keg details`() {
    assertEquals(
      Item(
        name = "Juicebox",
        keg = true,
        perItemPrice = 35.00,
        abv = 5.9,
        sizeMl = 5000,
        available = true,
        thumbnailUrl = URI("https://www.fourpure.com/uploads/images/products/thumbs/fourpurebrewingco._fourpure_juicebox5lminikeg_1588779669WhatsAppImage20200506at14.07.452.jpeg")
      ),
      ITEMS.first { it.name == "Juicebox" && it.keg }.noDesc()
    )
  }

  @Test
  fun `extracts description`() {
    assertNotNull(ITEMS.byName("Basecamp").desc)
  }
}

