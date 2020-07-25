package watch.craft.scrapers

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import watch.craft.*
import watch.craft.Format.BOTTLE
import watch.craft.Scraper.Node.ScrapedItem
import java.net.URI

class CraftyScraperTest {
  companion object {
    private val ITEMS = executeScraper(CraftyScraper(), dateString = "2020-07-15")
  }

  @Test
  fun `finds all the beers`() {
    assertEquals(17, ITEMS.size)
  }

  @Test
  fun `extracts beer details`() {
    assertEquals(
      ScrapedItem(
        name = "Heady Steady Go APA",
        abv = 4.5,
        offers = setOf(
          Offer(totalPrice = 6.00, quantity = 6, sizeMl = 500, format = BOTTLE),
          Offer(totalPrice = 12.00, quantity = 12, sizeMl = 500, format = BOTTLE),
          Offer(totalPrice = 24.00, quantity = 24, sizeMl = 500, format = BOTTLE)
        ),
        available = true,
        thumbnailUrl = URI("https://www.craftybrewing.co.uk/wp-content/uploads/2020/03/crafty-brewing-heady-steady-go-1-500x500.jpg")
      ),
      ITEMS.byName("Heady Steady Go APA").noDesc()
    )
  }

  @Test
  fun `extracts description`() {
    assertNotNull(ITEMS.byName("Heady Steady Go APA").desc)
  }

  @Test
  fun `identifies mixed`() {
    val item = ITEMS.byName("Crafty Dozen")

    assertTrue(item.mixed)
    assertEquals(12, item.onlyOffer().quantity)
    assertEquals(27.50, item.onlyOffer().totalPrice)
  }

  @Test
  fun `identifies sold-out`() {
    assertFalse(ITEMS.byName("Dunsfold Best Mild Ale").available)
  }
}

