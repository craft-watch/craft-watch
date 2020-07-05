package watch.craft.scrapers

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import watch.craft.Scraper.ScrapedItem
import watch.craft.byName
import watch.craft.display
import watch.craft.executeScraper
import watch.craft.noDesc
import java.net.URI

class RedWillowScraperTest {
  companion object {
    private val ITEMS = executeScraper(RedWillowScraper(), dateString = null)
  }

  @Test
  fun `finds all the beers`() {
    ITEMS.display()
//    assertEquals(7, ITEMS.size)
  }

//  @Test
//  fun `extracts beer details`() {
//    assertEquals(
//      ScrapedItem(
//        name = "Sundance",
//        summary = "India Pale Ale",
//        abv = 5.6,
//        sizeMl = 330,
//        price = 34.00,
//        numItems = 12,
//        available = true,
//        thumbnailUrl = URI("https://images.squarespace-cdn.com/content/v1/5073f3b284ae5bd7fb72db78/1593764819140-LYD57TK416V65H35KU9K/ke17ZwdGBToddI8pDm48kH7bHF972s0BMy8FJS7_5eh7gQa3H78H3Y0txjaiv_0fDoOvxcdMmMKkDsyUqMSsMWxHk725yiiHCCLfrh8O1z5QPOohDIaIeljMHgDF5CVlOqpeNLcJ80NK65_fV7S1Udkb9EYnQod1oo25oIrfEHqqJxs3PTnNIpDd2Nj8pGws6ary7BfG6-GuG8rvj_sKiw/Sundance330.jpg")
//      ),
//      ITEMS.byName("Sundance").noDesc()
//    )
//  }
//
//  @Test
//  fun `extracts description`() {
//    assertNotNull(ITEMS.byName("Sundance").desc)
//  }
//
//  @Test
//  fun `identifies mixed cases`() {
//    val item = ITEMS.byName("Mixed")
//    assertTrue(item.mixed)
//    assertEquals(12, item.numItems)
//  }
//
//  @Test
//  fun `identifies sold out`() {
//    assertFalse(ITEMS.first { it.name == "Sundance" && it.sizeMl == 440 }.available)
//  }
}

