package watch.craft.scrapers

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import watch.craft.Format.CAN
import watch.craft.Format.KEG
import watch.craft.Offer
import watch.craft.Scraper.Node.ScrapedItem
import watch.craft.byName
import watch.craft.dsl.containsMatch
import watch.craft.executeScraper
import watch.craft.noDesc
import java.net.URI

class AffinityScraperTest {
  companion object {
    private val ITEMS = executeScraper(AffinityScraper(), dateString = "2020-07-25")
  }

  @Test
  fun `finds all the beers`() {
    assertEquals(9, ITEMS.size)
  }

  @Test
  fun `extracts beer details`() {
    assertEquals(
      ScrapedItem(
        name = "Paper Mountains",
        abv = 6.7,
        offers = setOf(
          Offer(quantity = 1, totalPrice = 5.00, sizeMl = 440, format = CAN)
        ),
        available = true,
        thumbnailUrl = URI("https://affinitybrewco.com/____impro/1/webshopmedia/papermountains-1589800014579.jpg"),
        url = URI("https://affinitybrewco.com/shop.html#!/products/paper-mountains-440ml")
      ),
      ITEMS.byName("Paper Mountains").noDesc()
    )
  }

  @Test
  fun `extracts description and cleanses HTML tags`() {
    val item = ITEMS.byName("Paper Mountains")

    assertNotNull(item.desc)
    assertFalse(item.desc!!.containsMatch("<.*>"))
  }

  @Test
  fun `multiple offers`() {
    assertEquals(
      setOf(
        Offer(quantity = 1, totalPrice = 4.50, sizeMl = 440, format = CAN),
        Offer(quantity = 6, totalPrice = 25.00, sizeMl = 440, format = CAN)
      ),
      ITEMS.byName("Crumble").offers
    )
  }

  @Test
  fun `ignores non-beers`() {
    assertFalse(ITEMS.any { it.name.contains("shirt", ignoreCase = true) })
  }

  @Test
  fun `removes nonsense from names`() {
    assertFalse(ITEMS.any { it.name.contains("keg", ignoreCase = true) })
    assertFalse(ITEMS.any { it.name.contains("new", ignoreCase = true) })
    assertFalse(ITEMS.any { it.name.contains("\\d".toRegex()) })
  }

  @Test
  fun `identifies kegs`() {
    assertFalse(ITEMS.flatMap { it.offers }.any { it.sizeMl == 5000 && it.format != KEG })
  }
}

