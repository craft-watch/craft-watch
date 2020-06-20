package choliver.neapi.scrapers

import choliver.neapi.ParsedItem
import choliver.neapi.executeScraper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.net.URI

class GipsyHillScraperTest {
  companion object {
    private val ITEMS = executeScraper(GipsyHillScraper())
  }

  @Test
  fun `finds all the beers`() {
    assertEquals(18, ITEMS.size)
  }

  @Test
  fun `eliminates duplicates`() {
    assertEquals(1, ITEMS.filter { it.name == "Moneybags" }.size)
  }

  @Test
  fun `extracts beer details`() {
    assertTrue(
      ParsedItem(
        name = "Carver",
        price = "2.20".toBigDecimal(),
        abv = "2.8".toBigDecimal(),
        available = true,
        thumbnailUrl = URI("https://i1.wp.com/gipsyhillbrew.com/wp-content/uploads/2018/11/CARVER.png?resize=300%2C300&ssl=1"),
        url = URI("https://gipsyhillbrew.com/product/carver/")
      ) in ITEMS
    )
  }
}

