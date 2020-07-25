package watch.craft.scrapers

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import watch.craft.*
import watch.craft.Format.KEG
import watch.craft.Scraper.Node.ScrapedItem
import java.net.URI
import kotlin.text.RegexOption.IGNORE_CASE

class FourpureScraperTest {
  companion object {
    private val ITEMS = executeScraper(FourpureScraper())
  }

  @Test
  fun `finds all the beers`() {
    assertEquals(15, ITEMS.size)
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
      ScrapedItem(
        name = "Basecamp",
        offers = setOf(
          Offer(totalPrice = 2.00, sizeMl = 330)
        ),
        abv = 4.7,
        available = true,
        thumbnailUrl = URI("https://www.fourpure.com/uploads/images/products/thumbs/fourpurebrewingco._fourpure_pilslager_1566986321BASECAMPNB.png")
      ),
      ITEMS.byName("Basecamp").noDesc()
    )
  }

  @Test
  fun `extracts keg details`() {
    assertEquals(
      ScrapedItem(
        name = "Juicebox",
        offers = setOf(
          Offer(totalPrice = 35.00, format = KEG, sizeMl = 5000)
        ),
        abv = 5.9,
        available = true,
        thumbnailUrl = URI("https://www.fourpure.com/uploads/images/products/thumbs/fourpurebrewingco._fourpure_juicebox5lminikeg_1588779669WhatsAppImage20200506at14.07.452.jpeg")
      ),
      ITEMS.first { it.name == "Juicebox" && it.onlyOffer().format == KEG }.noDesc()
    )
  }

  @Test
  fun `extracts description`() {
    assertNotNull(ITEMS.byName("Basecamp").desc)
  }
}

