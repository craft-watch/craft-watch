package watch.craft.scrapers

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import watch.craft.*
import watch.craft.Format.BOTTLE
import watch.craft.Format.CAN
import watch.craft.Scraper.Node.ScrapedItem
import java.net.URI

class WildBeerScraperTest {
  companion object {
    private val ITEMS = executeScraper(WildBeerScraper(), dateString = "2020-07-24")
  }

  @Test
  fun `finds all the beers`() {
    assertEquals(38, ITEMS.size)
  }

  @Test
  fun `extracts beer details`() {
    assertEquals(
      ScrapedItem(
        name = "Coolship 2020",
        summary = "Spontaneous + Pale + Time",
        abv = 5.9,
        offers = setOf(
          Offer(quantity = 1, totalPrice = 18.00, sizeMl = 750, format = BOTTLE)
        ),
        available = true,
        thumbnailUrl = URI("https://www.wildbeerco.com/uploads/images/products/range/wild-beer-co-coolship-2020-1589815689Coolship-2020.png")
      ),
      ITEMS.byName("Coolship 2020").noDesc()
    )
  }

  @Test
  fun `extracts description`() {
    assertNotNull(ITEMS.byName("Coolship 2020").desc)
  }

  @Test
  fun `identifies sold-out and falls back to basic price info`() {
    val item = ITEMS.byName("Whitewood")

    assertFalse(item.available)
    assertEquals(
      Offer(quantity = 1, totalPrice = 7.00, sizeMl = 750, format = BOTTLE),
      item.onlyOffer()
    )
  }

  @Test
  fun `cleans up names`() {
    assertFalse(ITEMS.any { it.name.contains("keg", ignoreCase = true) })
  }

  @Test
  fun `extracts multiple offers`() {
    assertEquals(
      setOf(
        Offer(quantity = 1, totalPrice = 2.30, sizeMl = 330, format = CAN),
        Offer(quantity = 6, totalPrice = 12.50, sizeMl = 330, format = CAN),
        Offer(quantity = 12, totalPrice = 24.00, sizeMl = 330, format = CAN),
        Offer(quantity = 24, totalPrice = 45.00, sizeMl = 330, format = CAN)
      ),
      ITEMS.byName("Sleeping Limes").offers
    )
  }
}

