package choliver.neapi.scrapers

import choliver.neapi.ParsedItem
import org.jsoup.Jsoup
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.net.URI

class VillagesScraperTest {
  private val raw = {}.javaClass.getResource("/samples/villages.html").readText()
  private val doc = Jsoup.parse(raw)
  private val items = VillagesScraper().scrape(doc)

  @Test
  fun `finds all the beers`() {
    assertEquals(7, items.size)
  }

  @Test
  fun `extracts beer details`() {
    assertTrue(
      ParsedItem(
        name = "RODEO Pale Ale 4.6%",
        price = "25.60".toBigDecimal(),
        available = true,
        url = URI("/collections/buy-beer/products/rodeo-pale-ale")
      ) in items
    )
  }
}

