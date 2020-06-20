package choliver.neapi

import org.jsoup.Jsoup
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.net.URI

class GipsyHillScraperTest {
  private val raw = {}.javaClass.getResource("/gipsy-hill.html").readText()
  private val doc = Jsoup.parse(raw)
  private val items = GipsyHillScraper().scrape(doc)

  @Test
  fun `finds all the beers`() {
    assertEquals(18, items.size)
  }

  @Test
  fun `eliminates duplicates`() {
    assertEquals(1, items.filter { it.name == "Moneybags" }.size)
  }

  @Test
  fun `extracts beer details`() {
    assertTrue(
      ParsedItem(
        name = "Carver",
        price = "2.20".toBigDecimal(),
        available = true,
        url = URI("https://gipsyhillbrew.com/product/carver/")
      ) in items
    )
  }
}

