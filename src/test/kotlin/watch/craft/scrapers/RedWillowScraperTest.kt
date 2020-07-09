package watch.craft.scrapers

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import watch.craft.*
import watch.craft.Scraper.ScrapedItem
import java.net.URI

class RedWillowScraperTest {
  companion object {
    private val ITEMS = executeScraper(RedWillowScraper(), dateString = null)
  }

  @Test
  fun `finds all the beers`() {
    assertEquals(13, ITEMS.size)
  }

  @Test
  fun `extracts beer details`() {
    assertEquals(
      ScrapedItem(
        name = "Contactless",
        summary = "Hazy IPA",
        abv = 5.2,
        offers = setOf(
          Offer(totalPrice = 20.00, quantity = 6)
        ),
        available = true,
        thumbnailUrl = URI("https://images.squarespace-cdn.com/content/v1/57876558f5e231f300f4e5d4/1592322271269-NZD777HQ0EWLCDZYVYER/ke17ZwdGBToddI8pDm48kIu1QHqWqYcqSXGQukjVMYR7gQa3H78H3Y0txjaiv_0fDoOvxcdMmMKkDsyUqMSsMWxHk725yiiHCCLfrh8O1z4YTzHvnKhyp6Da-NYroOW3ZGjoBKy3azqku80C789l0k5fwC0WRNFJBIXiBeNI5fIG4lvOJgCZoc7R0Cd0Owykpqjz4OyDzVxk_JLua2qGvQ/Contactless+.jpg?format=200w")
      ),
      ITEMS.byName("Contactless").noDesc()
    )
  }

  @Test
  fun `extracts description`() {
    assertNotNull(ITEMS.byName("Contactless").desc)
  }

  @Test
  fun `identifies mixed cases`() {
    val items = ITEMS.filter { it.mixed }

    assertFalse(items.isEmpty())
    assertFalse(items.any { it.onlyOffer().quantity == 1 })
  }

  @Test
  fun `falls back to identifying number of items from title`() {
    assertEquals(12, ITEMS.byName("Pale Mixed Case").onlyOffer().quantity)
  }

  @Test
  fun `ignores non-beers`() {
    assertFalse(ITEMS.any { it.name.contains("glass", ignoreCase = true) })
  }
}

