package watch.craft.scrapers

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import watch.craft.Offer
import watch.craft.Scraper.ScrapedItem
import watch.craft.byName
import watch.craft.executeScraper
import watch.craft.noDesc
import java.net.URI

class BoxcarScraperTest {
  companion object {
    private val ITEMS = executeScraper(BoxcarScraper())
  }

  @Test
  fun `finds all the beers`() {
    assertEquals(8, ITEMS.size)
  }

  @Test
  fun `extracts available beers`() {
    assertEquals(
      ScrapedItem(
        name = "Dreamful",
        summary = "IPA",
        sizeMl = 440,
        abv = 6.5,
        offers = setOf(Offer(totalPrice = 4.95)),
        available = true,
        thumbnailUrl = URI("https://cdn.shopify.com/s/files/1/0358/6742/6953/products/IMG-20200604-WA0003_345x345.jpg?v=1591345860")
      ),
      ITEMS.byName("Dreamful").noDesc()
    )
  }

  @Test
  fun `extracts unavailable beers`() {
    assertEquals(
      ScrapedItem(
        name = "Dark Mild",
        sizeMl = 440,
        abv = 3.6,
        offers = setOf(Offer(totalPrice = 3.75)),
        available = false,
        thumbnailUrl = URI("https://cdn.shopify.com/s/files/1/0358/6742/6953/products/20200429_183043_345x345.jpg?v=1588181467")
      ),
      ITEMS.byName("Dark Mild").noDesc()
    )
  }

  @Test
  fun `extracts description`() {
    assertNotNull(ITEMS.byName("Dreamful").desc)
  }
}

