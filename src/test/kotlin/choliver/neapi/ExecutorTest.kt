package choliver.neapi

import choliver.neapi.Scraper.IndexEntry
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.net.URI

class ExecutorTest {
  private val getter = mock<HttpGetter>()
  private val executor = Executor(getter)

  @Test
  fun `scrapes multiple products`() {
    val brewery = "Foo Bar"
    val rootUrl = URI("https://example.invalid/shop")
    val productAUrl = URI("https://eaxmple.invalid/sweet-ipa")
    val productBUrl = URI("https://eaxmple.invalid/ted-shandy")
    val thumbnailAUrl = URI("https://example.invalid/assets/sweet-ipa.jpg")
    val thumbnailBUrl = URI("https://example.invalid/assets/ted-shandy.jpg")

    val itemA = ScrapedItem(
      name = "Sweet IPA",
      summary = "Bad ass",
      perItemPrice = 4.23,
      sizeMl = 440,
      abv = 6.9,
      available = true,
      thumbnailUrl = thumbnailAUrl
    )

    val itemB = ScrapedItem(
      name = "Ted Shandy",
      summary = "Awful",
      perItemPrice = 1.86,
      sizeMl = 330,
      abv = 1.2,
      available = true,
      thumbnailUrl = thumbnailBUrl
    )

    val scraper = mock<Scraper>()
    whenever(scraper.name) doReturn brewery
    whenever(scraper.rootUrl) doReturn rootUrl
    whenever(scraper.scrapeIndex(any())) doReturn listOf(
      IndexEntry(productAUrl) { itemA },
      IndexEntry(productBUrl) { itemB }
    )

    whenever(getter.get(rootUrl)) doReturn ""
    whenever(getter.get(productAUrl)) doReturn ""
    whenever(getter.get(productBUrl)) doReturn ""

    assertEquals(
      Inventory(
        listOf(
          Item(
            brewery = brewery,
            name = "Sweet IPA",
            summary = "Bad ass",
            sizeMl = 440,
            abv = 6.9,
            perItemPrice = 4.23,
            available = true,
            thumbnailUrl = thumbnailAUrl.toString(),
            url = productAUrl.toString()
          ),
          Item(
            brewery = brewery,
            name = "Ted Shandy",
            summary = "Awful",
            sizeMl = 330,
            abv = 1.2,
            perItemPrice = 1.86,
            available = true,
            thumbnailUrl = thumbnailBUrl.toString(),
            url = productBUrl.toString()
          )
        )
      ),
      executor.scrapeAll(scraper)
    )
  }
}
