package choliver.neapi.scrapers

import choliver.neapi.ParsedItem
import choliver.neapi.executeScraper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.net.URI
import kotlin.text.RegexOption.*

class FourpureScraperTest {
  companion object {
    private val ITEMS = executeScraper(FourpureScraper())
  }

  @Test
  fun `finds all the beers`() {
    assertEquals(8, ITEMS.size)
  }

  @Test
  fun `excludes kegs and packs`() {
    assertTrue(ITEMS.none { it.name.contains("keg|pack".toRegex(IGNORE_CASE)) })
  }

  @Test
  fun `strips size from name`() {
    assertTrue(ITEMS.none { it.name.contains("ml", ignoreCase = true) })
  }

  @Test
  fun `extracts beer details`() {
    assertEquals(
      ParsedItem(
        name = "Basecamp",
        pricePerCan = "2.00".toBigDecimal(),
        abv = "4.7".toBigDecimal(),
        sizeMl = 330,
        available = true,
        thumbnailUrl = URI("https://www.fourpure.com/uploads/images/products/thumbs/fourpurebrewingco._fourpure_pilslager_1566986321BASECAMPNB.png"),
        url = URI("https://www.fourpure.com/item/3/Fourpure/Basecamp.html")
      ),
      ITEMS.first { it.name == "Basecamp" }
    )
  }
}

