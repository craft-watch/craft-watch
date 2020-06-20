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
        name = "RODEO Pale Ale",
        abv = "4.6".toBigDecimal(),
        price = "25.60".toBigDecimal(),
        available = true,
        url = URI("/collections/buy-beer/products/rodeo-pale-ale")
      ) in items
    )
  }

  @Test
  fun `extracts case details`() {
    assertTrue(
      ParsedItem(
        name = "VILLAGES Mixed Case (24 × cans)",
        abv = null,   // Can't find this!
        price = "52.50".toBigDecimal(),
        available = true,
        url = URI("/collections/buy-beer/products/villages-mixed-case")
      ) in items
    )
  }
}

