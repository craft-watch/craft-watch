package choliver.neapi.scrapers

import choliver.neapi.ParsedItem
import org.jsoup.Jsoup
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.net.URI

class BoxcarScraperTest {
  private val raw = {}.javaClass.getResource("/samples/boxcar.html").readText()
  private val doc = Jsoup.parse(raw)
  private val items = BoxcarScraper().scrape(doc)

  @Test
  fun `finds all the beers`() {
    assertEquals(8, items.size)
  }

  @Test
  fun `extracts available beers`() {
    assertTrue(
      ParsedItem(
        name = "Dreamful // 6.5% IPA // 440ml",
        price = "4.95".toBigDecimal(),
        available = true,
        url = URI("/collections/beer/products/dreamful-6-5-ipa-440ml")  // TODO - must normalise
      ) in items
    )
  }

  @Test
  fun `extracts unavailable beers`() {
    assertTrue(
      ParsedItem(
        name = "Dark Mild // 3.6% // 440ml",
        price = "3.75".toBigDecimal(),
        available = false,
        url = URI("/collections/beer/products/dark-mild")  // TODO - must normalise
      ) in items
    )
  }
}

