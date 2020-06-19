package choliver.neapi

import org.jsoup.Jsoup
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.net.URI

class VillagesParserTest {
  private val raw = {}.javaClass.getResource("/villages.html").readText()
  private val doc = Jsoup.parse(raw)
  private val items = VillagesParser().parse(doc)

  @Test
  fun `finds all the beers`() {
    assertEquals(7, items.size)
  }

  @Test
  fun `extracts beer details`() {
    assertTrue(
      Item(
        name = "RODEO Pale Ale 4.6%",
        price = "25.60".toBigDecimal(),
        available = true,
        url = URI("/collections/buy-beer/products/rodeo-pale-ale")
      ) in items
    )
  }
}

