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

class PressureDropScraperTest {
  companion object {
    private val ITEMS = executeScraper(PressureDropScraper())
  }

  @Test
  fun `finds all the beers`() {
    assertEquals(9, ITEMS.size)
  }

  @Test
  fun `extracts beer details`() {
    assertEquals(
      ScrapedItem(
        name = "Pale Fire",
        summary = "Mosaic & Amarillo Pale",
        abv = 4.8,
        offers = setOf(
          Offer(totalPrice = 3.70, sizeMl = 440)
        ),
        available = true,
        thumbnailUrl = URI("https://cdn.shopify.com/s/files/1/0173/0153/6832/products/IMG_7845_large.jpg?v=1588861254")
      ),
      ITEMS.byName("Pale Fire").noDesc()
    )
  }

  @Test
  fun `ignores boxes`() {
    assertEquals(
      emptyList<String>(),
      ITEMS.map { it.name }.filter { it.contains("box", ignoreCase = true) }
    )
  }

  @Test
  fun `extracts description`() {
    assertNotNull(ITEMS.byName("Dreamlife").desc)
  }
}

