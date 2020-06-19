package choliver.neapi

import org.jsoup.Jsoup
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.net.URI

class HowlingHopsParserTest {
  private val raw = {}.javaClass.getResource("/howling-hops.html").readText()
  private val doc = Jsoup.parse(raw)
  private val items = HowlingHopsParser().parse(doc)

  @Test
  fun `finds all the beers`() {
    assertEquals(17, items.size)
  }

  @Test
  fun `extracts sale price not original price`() {
    assertTrue(
      Item(
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

