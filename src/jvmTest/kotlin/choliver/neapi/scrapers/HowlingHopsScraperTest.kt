package choliver.neapi.scrapers

import choliver.neapi.ParsedItem
import choliver.neapi.executeScraper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.net.URI

class HowlingHopsScraperTest {
  companion object {
    private val ITEMS = executeScraper(HowlingHopsScraper())
  }

  @Test
  fun `finds all the beers`() {
    assertEquals(17, ITEMS.size)
  }

  @Test
  fun `extracts sale price not original price`() {
    assertTrue(
      ParsedItem(
        name = "Push Push DDH Pale 4 x 440ml",
        price = "16.00".toBigDecimal(),
        abv = "5.8".toBigDecimal(),
        sizeMl = 440,
        available = true,
        thumbnailUrl = URI("https://www.howlinghops.co.uk/wp-content/uploads/2020/06/push-push-440ml-324x324.png"),
        url = URI("https://www.howlinghops.co.uk/product/push-push-4-x-440ml/")
      ) in ITEMS
    )
  }

  @Test
  fun `doesn't extract apparel`() {
    assertTrue(
      ITEMS.none { it.name.contains("various colours") }
    )
  }
}

