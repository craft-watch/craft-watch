package choliver.neapi.scrapers

import choliver.neapi.ParsedItem
import org.jsoup.Jsoup
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.net.URI

class HowlingHopsScraperTest {
  private val raw = {}.javaClass.getResource("/samples/howling-hops.html").readText()
  private val doc = Jsoup.parse(raw)
  private val items = HowlingHopsScraper().scrape(doc)

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

