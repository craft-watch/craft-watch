package watch.craft.executor

import com.nhaarman.mockitokotlin2.*
import org.jsoup.nodes.Document
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import watch.craft.*
import watch.craft.Scraper.IndexEntry
import watch.craft.storage.CachingGetter
import java.net.URI
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

class ExecutorTest {
  private val getter = mock<CachingGetter> {
    on { request(any()) } doAnswer { "<html><body><h1>${it.getArgument<URI>(0)}</h1></body></html>".toByteArray() }
  }
  private val executor = Executor(getter, Clock.fixed(NOW, ZoneId.systemDefault()))
  private val scraper = mock<Scraper> {
    on { name } doReturn BREWERY
    on { rootUrls } doReturn listOf(ROOT_URL_BEERS, ROOT_URL_PACKS)
  }

  @Test
  fun `passes correct URLs and HTML around`() {
    val callback = mock<(Document) -> Scraper.ScrapedItem> {
      on { invoke(any()) } doThrow SkipItemException("Emo town")
    }
    whenever(scraper.scrapeIndex(any())) doReturnConsecutively listOf(
      listOf(indexEntry("a", callback)),
      listOf(indexEntry("b", callback))
    )

    executor.scrape(scraper)

    verify(getter).request(ROOT_URL_BEERS)
    verify(getter).request(ROOT_URL_PACKS)
    verify(getter).request(productUrl("a"))
    verify(getter).request(productUrl("b"))
    verify(scraper).scrapeIndex(docWithHeaderMatching(ROOT_URL_BEERS.toString()))
    verify(scraper).scrapeIndex(docWithHeaderMatching(ROOT_URL_PACKS.toString()))
    verify(callback)(docWithHeaderMatching(productUrl("a").toString()))
    verify(callback)(docWithHeaderMatching(productUrl("b").toString()))
  }

  @Test
  fun `scrapes products`() {
    whenever(scraper.scrapeIndex(any())) doReturn listOf(
      indexEntry("a") { product("Foo") },
      indexEntry("b") { product("Bar") }
    )

    assertEquals(
      listOf(
        with(product("Bar")) {
          Item(
            brewery = BREWERY,
            name = name,
            summary = summary,
            desc = desc,
            keg = keg,
            mixed = mixed,
            sizeMl = sizeMl,
            abv = abv,
            perItemPrice = price,
            available = available,
            thumbnailUrl = thumbnailUrl.toString(),
            url = productUrl("b").toString()
          )
        },
        with(product("Foo")) {
          Item(
            brewery = BREWERY,
            name = name,
            summary = summary,
            desc = desc,
            keg = keg,
            mixed = mixed,
            sizeMl = sizeMl,
            abv = abv,
            perItemPrice = price,
            available = available,
            thumbnailUrl = thumbnailUrl.toString(),
            url = productUrl("a").toString()
          )
        }
      ),
      executor.scrape(scraper).items
    )
  }

  @Test
  fun `filters out skipped results`() {
    whenever(scraper.scrapeIndex(any())) doReturn listOf(
      indexEntry("a") { throw SkipItemException("Just too emo") },
      indexEntry("b") { product("Foo") },
      indexEntry("c") { throw SkipItemException("Made of cheese") }
    )

    // Only one item returned
    assertEquals(
      listOf("Foo"),
      executor.scrape(scraper).items.map { it.name }
    )
  }

  @Test
  fun `de-duplicates by picking best price`() {
    whenever(scraper.scrapeIndex(any())) doReturn listOf(
      indexEntry("a") { product("Foo") },
      indexEntry("b") { product("Foo").copy(price = DECENT_PRICE / 2) },
      indexEntry("c") { product("Foo").copy(price = DECENT_PRICE * 2) }
    )

    // Only one item returned, with best price
    assertEquals(
      listOf(DECENT_PRICE / 2),
      executor.scrape(scraper).items.map { it.perItemPrice }
    )
  }

  @Test
  fun `dies on fatal index-scrape failure`() {
    whenever(scraper.scrapeIndex(any())) doThrow FatalScraperException("Noooo")

    assertThrows<FatalScraperException> {
      executor.scrape(scraper)
    }
  }

  @Test
  fun `dies on fatal item-scrape failure`() {
    whenever(scraper.scrapeIndex(any())) doReturn listOf(
      indexEntry("a") { throw FatalScraperException("Noooo") }
    )

    assertThrows<FatalScraperException> {
      executor.scrape(scraper)
    }
  }

  @Test
  fun `continues after non-fatal index-scrape failure`() {
    whenever(scraper.scrapeIndex(any())) doReturn listOf(
      indexEntry("a") { product("Foo") },
      indexEntry("b") { product("Bar") }
    )

    val badScraper = mock<Scraper> {
      on { name } doReturn "Bad Brewing"
      on { rootUrls } doReturn listOf(URI("http://bad.invalid"))
      on { scrapeIndex(any()) } doThrow MalformedInputException("Noooo")
    }

    assertEquals(
      listOf("Bar", "Foo"),
      executor.scrape(badScraper, scraper).items.map { it.name } // Execute good and bad scrapers
    )
  }

  @Test
  fun `continues after non-fatal item-scrape failure`() {
    whenever(scraper.scrapeIndex(any())) doReturn listOf(
      indexEntry("a") { product("Foo") },
      indexEntry("b") { throw MalformedInputException("What happened") },
      indexEntry("c") { product("Bar") }
    )

    assertEquals(
      listOf("Bar", "Foo"),
      executor.scrape(scraper).items.map { it.name }
    )
  }

  @Test
  fun `continues after validation failure`() {
    whenever(scraper.scrapeIndex(any())) doReturn listOf(
      indexEntry("a") { product("Foo") },
      indexEntry("b") { product("Foo").copy(name = "") },  // Invalid name
      indexEntry("c") { product("Bar") }
    )

    assertEquals(
      listOf("Bar", "Foo"),
      executor.scrape(scraper).items.map { it.name }
    )
  }

  companion object {
    private const val BREWERY = "Foo Bar"
    private val ROOT_URL_BEERS = URI("https://example.invalid/beers")
    private val ROOT_URL_PACKS = URI("https://example.invalid/packs")
    private const val DECENT_PRICE = 2.46
    private val NOW = Instant.EPOCH

    private fun product(name: String) = Scraper.ScrapedItem(
      name = name,
      summary = "${name} is great",
      price = DECENT_PRICE,
      sizeMl = 440,
      abv = 6.9,
      available = true,
      thumbnailUrl = URI("https://example.invalid/assets/${name}.jpg")
    )

    private fun indexEntry(suffix: String, scrapeItem: (doc: Document) -> Scraper.ScrapedItem) =
      IndexEntry(suffix, productUrl(suffix), scrapeItem)

    private fun productUrl(suffix: String) = URI("https://eaxmple.invalid/${suffix}")

    private fun docWithHeaderMatching(header: String): Document = argForWhich { textFrom("h1") == header }
  }
}
