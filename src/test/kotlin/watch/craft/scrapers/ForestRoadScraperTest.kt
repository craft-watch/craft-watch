package watch.craft.scrapers

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import watch.craft.*
import watch.craft.Format.BOTTLE
import watch.craft.Format.KEG
import watch.craft.Scraper.ScrapedItem
import java.net.URI

class ForestRoadScraperTest {
  companion object {
    private val ITEMS = executeScraper(ForestRoadScraper(), dateString = "2020-07-13")
  }

  @Test
  fun `finds all the beers`() {
    ITEMS.display()
    assertEquals(10, ITEMS.size)
  }

  @Test
  fun `extracts beer details`() {
    assertEquals(
      ScrapedItem(
        name = "Work IPA",
        summary = "India Pale Ale",
        abv = 5.4,
        offers = setOf(
          Offer(totalPrice = 26.00, quantity = 12, sizeMl = 330, format = BOTTLE)
        ),
        available = true,
        thumbnailUrl = URI("https://static1.squarespace.com/static/5ca0c8b83560c3098a94e46b/5e70a00b24050371dc9942a8/5e70ac6f8e693374038081dd/1594035000018/")
      ),
      ITEMS.first { it.name == "Work IPA" && it.onlyOffer().format == BOTTLE }.noDesc()
    )
  }

  @Test
  fun `extracts description`() {
    assertNotNull(ITEMS.byName("Work IPA").desc)
  }

  @Test
  fun `identifies keg`() {
    val item = ITEMS.byName("Party Keg")

    assertEquals(KEG, item.onlyOffer().format)
    assertTrue(item.onlyOffer().sizeMl!! >= 1000)
  }

  @Test
  fun `identifies mixed`() {
    val item = ITEMS.byName("Mixed 12 Pack")

    assertTrue(item.mixed)
    assertTrue(item.onlyOffer().quantity > 1)
  }
}
