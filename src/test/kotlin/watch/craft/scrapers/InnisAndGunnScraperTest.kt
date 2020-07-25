package watch.craft.scrapers

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import watch.craft.Offer
import watch.craft.Scraper.Output.ScrapedItem
import watch.craft.byName
import watch.craft.executeScraper
import watch.craft.noDesc
import java.net.URI

class InnisAndGunnScraperTest {
  companion object {
    private val ITEMS = executeScraper(InnisAndGunnScraper(), dateString = "2020-07-12")
  }

  @Test
  fun `finds all the beers`() {
    assertEquals(5, ITEMS.size)
  }

  @Test
  fun `extracts beer details`() {
    assertEquals(
      ScrapedItem(
        name = "The Original",
        summary = "Barrel Aged",
        abv = 6.6,
        offers = setOf(
          Offer(quantity = 24, totalPrice = 34.99, sizeMl = 330)
        ),
        available = true,
        thumbnailUrl = URI("https://www.innisandgunn.com/uploads/images/products/range/innis-gunn-brewing-company-ltd-innis-gunn-the-original-330ml-bottle-case-1591011847I-G-Original-24-x-330-A.png")
      ),
      ITEMS.byName("The Original").noDesc()
    )
  }

  @Test
  fun `extracts description`() {
    assertNotNull(ITEMS.byName("The Original").desc)
  }
}

