package choliver.neapi

import choliver.neapi.Scraper.IndexEntry
import choliver.neapi.Scraper.Result
import choliver.neapi.getters.Getter
import com.nhaarman.mockitokotlin2.*
import org.jsoup.nodes.Document
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.net.URI

class ExecutorTest {
  private val getter = mock<Getter<String>> {
    on { request(any()) } doAnswer { "<html><body><h1>${it.getArgument<URI>(0)}</h1></body></html>" }
  }
  private val executor = Executor(getter)
  private val scraper = mock<Scraper> {
    on { name } doReturn BREWERY
    on { rootUrl } doReturn ROOT_URL
  }

  @Test
  fun `passes correct URLs and HTML around`() {
    val callback = mock<(Document) -> Result> {
      on { invoke(any()) } doReturn Result.Skipped("Emo town")
    }
    whenever(scraper.scrapeIndex(any())) doReturn listOf(
      indexEntry("a", callback)
    )

    executor.scrape(scraper)

    verify(getter).request(ROOT_URL)
    verify(getter).request(productUrl("a"))
    verify(scraper).scrapeIndex(docWithHeaderMatching(ROOT_URL.toString()))
    verify(callback)(docWithHeaderMatching(productUrl("a").toString()))
  }

  @Test
  fun `scrapes products`() {
    whenever(scraper.scrapeIndex(any())) doReturn listOf(
      indexEntry("a") { SWEET_IPA },
      indexEntry("b") { TED_SHANDY }
    )

    assertEquals(
      Inventory(
        listOf(
          with(SWEET_IPA) {
            Item(
              brewery = BREWERY,
              name = name,
              summary = summary,
              sizeMl = sizeMl,
              abv = abv,
              perItemPrice = perItemPrice,
              available = available,
              thumbnailUrl = thumbnailUrl.toString(),
              url = productUrl("a").toString()
            )
          },
          with (TED_SHANDY) {
            Item(
              brewery = BREWERY,
              name = name,
              summary = summary,
              sizeMl = sizeMl,
              abv = abv,
              perItemPrice = perItemPrice,
              available = available,
              thumbnailUrl = thumbnailUrl.toString(),
              url = productUrl("b").toString()
            )
          }
        )
      ),
      executor.scrape(scraper)
    )
  }

  @Test
  fun `filters out skipped results`() {
    whenever(scraper.scrapeIndex(any())) doReturn listOf(
      indexEntry("a") { Result.Skipped("Just too emo") },
      indexEntry("b") { SWEET_IPA },
      indexEntry("c") { Result.Skipped("Made of cheese") }
    )

    // Only one item returned
    assertEquals(
      listOf(SWEET_IPA.name),
      executor.scrape(scraper).items.map { it.name }
    )
  }

  @Test
  fun `de-duplicates by picking best price`() {
    whenever(scraper.scrapeIndex(any())) doReturn listOf(
      indexEntry("a") { SWEET_IPA },
      indexEntry("b") { SWEET_IPA.copy(perItemPrice = SWEET_IPA.perItemPrice / 2) },
      indexEntry("c") { SWEET_IPA.copy(perItemPrice = SWEET_IPA.perItemPrice * 2) }
    )

    // Only one item returned, with best price
    assertEquals(
      listOf(SWEET_IPA.perItemPrice / 2),
      executor.scrape(scraper).items.map { it.perItemPrice }
    )
  }

  @Test
  fun `index-scrape failure doesn't jettison everything`() {
    whenever(scraper.scrapeIndex(any())) doReturn listOf(
      indexEntry("a") { SWEET_IPA },
      indexEntry("b") { TED_SHANDY }
    )

    val badScraper = mock<Scraper> {
      on { name } doReturn "Bad Brewing"
      on { rootUrl } doReturn URI("http://bad.invalid")
      on { scrapeIndex(any()) } doThrow ScraperException("Noooo")
    }

    assertEquals(
      listOf(SWEET_IPA.name, TED_SHANDY.name),
      executor.scrape(badScraper, scraper).items.map { it.name } // Execute good and bad scrapers
    )
  }

  @Test
  fun `item-scrape failure doesn't jettison everything`() {
    whenever(scraper.scrapeIndex(any())) doReturn listOf(
      indexEntry("a") { SWEET_IPA },
      indexEntry("b") { throw ScraperException("What happened") },
      indexEntry("c") { TED_SHANDY }
    )

    assertEquals(
      listOf(SWEET_IPA.name, TED_SHANDY.name),
      executor.scrape(scraper).items.map { it.name }
    )
  }



  @Test
  fun `normalisation failure doesn't jettison everything`() {
    whenever(scraper.scrapeIndex(any())) doReturn listOf(
      indexEntry("a") { SWEET_IPA },
      indexEntry("b") { SWEET_IPA.copy(name = "") },  // Invalid name
      indexEntry("c") { TED_SHANDY }
    )

    assertEquals(
      listOf(SWEET_IPA.name, TED_SHANDY.name),
      executor.scrape(scraper).items.map { it.name }
    )
  }

  companion object {
    private const val BREWERY = "Foo Bar"
    private val ROOT_URL = URI("https://example.invalid/shop")

    private val SWEET_IPA = Result.Item(
      name = "Sweet IPA",
      summary = "Bad ass",
      perItemPrice = 4.23,
      sizeMl = 440,
      abv = 6.9,
      available = true,
      thumbnailUrl = URI("https://example.invalid/assets/sweet-ipa.jpg")
    )

    private val TED_SHANDY = Result.Item(
      name = "Ted Shandy",
      summary = "Awful",
      perItemPrice = 1.86,
      sizeMl = 330,
      abv = 1.2,
      available = true,
      thumbnailUrl = URI("https://example.invalid/assets/ted-shandy.jpg")
    )

    private fun indexEntry(suffix: String, scrapeItem: (doc: Document) -> Result) =
      IndexEntry(suffix, productUrl(suffix), scrapeItem)

    private fun productUrl(suffix: String) = URI("https://eaxmple.invalid/${suffix}")

    private fun docWithHeaderMatching(header: String): Document = argForWhich { textFrom("h1") == header }
  }
}
