package choliver.neapi

import org.jsoup.Jsoup
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.net.URI

class BoxcarParserTest {
  private val raw = {}.javaClass.getResource("/boxcar.html").readText()
  private val doc = Jsoup.parse(raw)
  private val items = BoxcarParser().parse(doc)

  @Test
  fun `finds all the beers`() {
    assertEquals(8, items.size)
  }

  @Test
  fun `extracts available beers`() {
    assertTrue(
      Item(
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
      Item(
        name = "Dark Mild // 3.6% // 440ml",
        price = "3.75".toBigDecimal(),
        available = false,
        url = URI("/collections/beer/products/dark-mild")  // TODO - must normalise
      ) in items
    )
  }
}

