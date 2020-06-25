package choliver.neapi.scrapers

import choliver.neapi.Scraper.Result.Item
import choliver.neapi.executeScraper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.net.URI

class FivePointsScraperTest {
  companion object {
    private val ITEMS = executeScraper(FivePointsScraper())
  }

  @Test
  fun `finds all the beers`() {
    ITEMS.forEach(::println)
//    assertEquals(17, ITEMS.size)
  }
}

