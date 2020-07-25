package watch.craft.scrapers

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import watch.craft.*
import watch.craft.Scraper.Output.ScrapedItem
import java.net.URI

class WiperAndTrueScraperTest {
  companion object {
    private val ITEMS = executeScraper(WiperAndTrueScraper())
  }

  @Test
  fun `finds all the beers`() {
    assertEquals(8, ITEMS.size)
  }

  @Test
  fun `extracts beer details`() {
    assertEquals(
      ScrapedItem(
        name = "Sundance",
        summary = "India Pale Ale",
        abv = 5.6,
        offers = setOf(
          Offer(totalPrice = 42.00, quantity = 12, sizeMl = 440)
        ),
        available = true,
        thumbnailUrl = URI("https://images.squarespace-cdn.com/content/v1/5073f3b284ae5bd7fb72db78/1594379894576-565E2K491OHV4T2W9ZQ8/ke17ZwdGBToddI8pDm48kN9tuRkJsx3DiD2Y7TzQo197gQa3H78H3Y0txjaiv_0fDoOvxcdMmMKkDsyUqMSsMWxHk725yiiHCCLfrh8O1z5QPOohDIaIeljMHgDF5CVlOqpeNLcJ80NK65_fV7S1URRp2ueNyHGG0bEVI4oGXrtB69HHrtaJ4VMk_06NPYpvWIB-7cQgYKo_bDpR6cEVkg/Wiper+%26+True+-6.jpg")
      ),
      ITEMS.first { it.name == "Sundance" }.noDesc()
    )
  }

  @Test
  fun `extracts description`() {
    assertNotNull(ITEMS.byName("Sundance").desc)
  }

  @Test
  fun `identifies mixed cases`() {
    val item = ITEMS.byName("Mixed")
    assertTrue(item.mixed)
    assertEquals(12, item.onlyOffer().quantity)
  }

  @Test
  fun `identifies sold out`() {
    assertFalse(ITEMS.first { it.name == "Small Beer" && it.onlyOffer().sizeMl == 440 }.available)
  }
}

