package watch.craft.scrapers

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import watch.craft.Format.CAN
import watch.craft.Offer
import watch.craft.Scraper.Output.ScrapedItem
import watch.craft.byName
import watch.craft.executeScraper
import watch.craft.noDesc
import java.net.URI

class PollysScraperTest {
  companion object {
    private val ITEMS = executeScraper(PollysScraper(), dateString = "2020-07-24")
  }

  @Test
  fun `finds all the beers`() {
    assertEquals(6, ITEMS.size)
  }

  @Test
  fun `extracts beer details`() {
    assertEquals(
      ScrapedItem(
        name = "Lupo Capisco",
        summary = "Pale Ale",
        abv = 5.6,
        offers = setOf(
          Offer(totalPrice = 4.25, sizeMl = 440, format = CAN)
        ),
        available = true,
        thumbnailUrl = URI("https://craftpeak-commerce-images.imgix.net/2020/07/Lupo-Capisco-01.png?auto=compress%2Cformat&fit=crop&h=324&ixlib=php-1.2.1&w=324&wpsize=woocommerce_thumbnail")
      ),
      ITEMS.byName("Lupo Capisco").noDesc()
    )
  }

  @Test
  fun `extracts description`() {
    assertNotNull(ITEMS.byName("Lupo Capisco").desc)
  }

  @Test
  fun `identifies sold out`() {
    assertFalse(ITEMS.byName("Circadian Rhythm").available)
  }
}

