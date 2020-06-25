package choliver.neapi.scrapers

import choliver.neapi.ScrapedItem
import choliver.neapi.executeScraper
import org.junit.jupiter.api.Assertions.assertEquals
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
    assertEquals(
      ScrapedItem(
        name = "Carver",
        summary = "Micro IPA",
        perItemPrice = 2.20,
        abv = 2.8,
        sizeMl = 330,
        available = true,
        thumbnailUrl = URI("https://i1.wp.com/gipsyhillbrew.com/wp-content/uploads/2018/11/CARVER.png?resize=300%2C300&ssl=1")
      ),
      ITEMS.first { it.name == "Carver" }
    )
  }

  @Test
  fun `normalises price for multi-pack`() {
    assertEquals(
      4.83,
      ITEMS.find { it.name == "Specials Box – 12 pack" }!!.perItemPrice
    )
  }
}

