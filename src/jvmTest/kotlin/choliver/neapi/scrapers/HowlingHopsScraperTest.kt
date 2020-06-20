package choliver.neapi.scrapers

import choliver.neapi.ParsedItem
import choliver.neapi.executeScraper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.net.URI

class HowlingHopsScraperTest {
  private val items = executeScraper(HowlingHopsScraper())

  @Test
  fun `finds all the beers`() {
    assertEquals(17, items.size)
  }

  @Test
  fun `extracts sale price not original price`() {
    assertTrue(
      ParsedItem(
        name = "NEW 12 Beer Mega Pack 24 x 440ml",
        price = "69.50".toBigDecimal(),
        available = true,
        thumbnailUrl = URI("https://www.howlinghops.co.uk/wp-content/uploads/2020/06/12beers_june2-324x324.jpg"),
        url = URI("https://www.howlinghops.co.uk/product/12-beer-mega-pack-24-x-440ml/")
      ) in items
    )
  }

  @Test
  fun `doesn't extract apparel`() {
    assertTrue(
      items.none { it.name.contains("various colours") }
    )
  }
}

