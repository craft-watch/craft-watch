package watch.craft.scrapers

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import watch.craft.Format.CAN
import watch.craft.Offer
import watch.craft.Scraper.ScrapedItem
import watch.craft.byName
import watch.craft.executeScraper
import watch.craft.noDesc
import java.net.URI

class RoostersScraperTest {
  companion object {
    private val ITEMS = executeScraper(RoostersScraper(), dateString = "2020-07-14")
  }

  @Test
  fun `finds all the beers`() {
    assertEquals(14, ITEMS.size)
  }

  @Test
  fun `extracts beer details`() {
    assertEquals(
      ScrapedItem(
        name = "Highway 51",
        summary = "Dry-Hopped Pale Ale",
        abv = 3.7,
        offers = setOf(
          Offer(quantity = 12, totalPrice = 19.20, sizeMl = 330, format = CAN),
          Offer(quantity = 24, totalPrice = 38.40, sizeMl = 330, format = CAN)
        ),
        available = true,
        thumbnailUrl = URI("https://static1.squarespace.com/static/5c40d56d365f021f9aeb091e/5c40ebd540ec9adbb47d32a3/5c9bc9bc3e50fb000182d414/1594110983863/")
      ),
      ITEMS.byName("Highway 51").noDesc()
    )
  }

  @Test
  fun `extracts description`() {
    assertNotNull(ITEMS.byName("Highway 51").desc)
  }

  @Test
  fun `identifies mixed`() {
    val items = ITEMS.filter { it.mixed }

    assertFalse(items.isEmpty())
    assertFalse(items.any { it.summary != null })
    assertFalse(items.flatMap { it.offers }.any { it.quantity == 1 })
  }

  @Test
  fun `ignores bag in box`() {
    assertFalse(ITEMS.any { it.name.contains("bag", ignoreCase = true) })
  }
}

