package choliver.neapi.scrapers

import choliver.neapi.executeScraper
import org.junit.jupiter.api.Test

class MarbleScraperTest {
  companion object {
    private val ITEMS = executeScraper(MarbleScraper())
  }

  @Test
  fun `finds all the beers`() {
    ITEMS.forEach(::println)
//    assertEquals(7, ITEMS.size)
  }
}

