package watch.craft.scrapers

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import watch.craft.*
import watch.craft.Format.CAN
import watch.craft.Format.KEG
import watch.craft.Scraper.Node.ScrapedItem
import watch.craft.dsl.containsWord
import java.net.URI

class ForestRoadScraperTest {
  companion object {
    private val ITEMS = executeScraper(ForestRoadScraper(), dateString = "2020-08-08")
  }

  @Test
  fun `finds all the beers`() {
    assertEquals(9, ITEMS.size)
  }

  @Test
  fun `extracts beer details`() {
    assertEquals(
      ScrapedItem(
        name = "Work IPA",
        abv = 5.4,
        offers = setOf(
          Offer(totalPrice = 27.00, quantity = 12, sizeMl = 330, format = CAN),
          Offer(totalPrice = 50.00, quantity = 24, sizeMl = 330, format = CAN)
        ),
        available = true,
        thumbnailUrl = URI("https://cdn.shopify.com/s/files/1/0019/2226/9245/products/workcan_200x.jpg")
      ),
      ITEMS.first { it.name == "Work IPA" && it.offers.any { offer -> offer.format == CAN } }.noDesc()
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
    val item = ITEMS.byName("Forest Road Mixed")

    assertTrue(item.mixed)
    assertTrue(item.onlyOffer().quantity > 1)
  }

  @Test
  fun `identifies sold-out`() {
    assertFalse(ITEMS.byName("Tart Winter Ale Ting").available)
  }

  @Test
  fun `ignores subscriptions`() {
    assertFalse(ITEMS.any { it.name.containsWord("subscription") })
  }
}
