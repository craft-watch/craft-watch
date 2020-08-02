package watch.craft.scrapers

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import watch.craft.*
import watch.craft.Format.CAN
import watch.craft.Scraper.Node.ScrapedItem
import java.net.URI

class AnspachAndHobdayScraperTest {
  companion object {
    private val ITEMS = executeScraper(AnspachAndHobdayScraper(), dateString = "2020-07-26")
  }

  @Test
  fun `finds all the beers`() {
    assertEquals(10, ITEMS.size)
  }

  @Test
  fun `extracts beer details`() {
    assertEquals(
      ScrapedItem(
        name = "The Lemons & The Limes",
        summary = "Anspach & Hobday X Affinity Brew Co",
        abv = 4.6,
        offers = setOf(
          Offer(quantity = 4, totalPrice = 10.25, sizeMl = 440, format = CAN),
          Offer(quantity = 12, totalPrice = 29.50, sizeMl = 440, format = CAN),
          Offer(quantity = 24, totalPrice = 56.95, sizeMl = 440, format = CAN)
        ),
        available = true,
        thumbnailUrl = URI("https://images.squarespace-cdn.com/content/v1/5b238a149f8770a79bf32145/1590656272973-EDBEGAYRH6PTTEDJYCIX/ke17ZwdGBToddI8pDm48kA5XaMz_zzBW3PwJxuY4MPB7gQa3H78H3Y0txjaiv_0fDoOvxcdMmMKkDsyUqMSsMWxHk725yiiHCCLfrh8O1z5QPOohDIaIeljMHgDF5CVlOqpeNLcJ80NK65_fV7S1UTmjR7j-uGvmKLPlQyBBZvZJNg_M93wufa1bwkIJvZ68QADPtwErjFPsenxaex4sAg/440+Can+Colour+Tile+-Lemons+%26+Limes.jpg?format=200w")
      ),
      ITEMS.byName("The Lemons & The Limes").noDesc()
    )
  }

  @Test
  fun `extracts description`() {
    assertNotNull(ITEMS.byName("The Lemons & The Limes").desc)
  }

  @Test
  fun `cleans up names`() {
    assertFalse(ITEMS.any { it.name.contains("\\d".toRegex()) })
    assertFalse(ITEMS.any { it.name.contains(":") })
  }

  @Test
  fun `ignores non-beers`() {
    assertFalse(ITEMS.any { it.name.contains("subscription", ignoreCase = true) })
    assertFalse(ITEMS.any { it.name.contains("shirt", ignoreCase = true) })
  }
}

