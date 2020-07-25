package watch.craft

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.fail
import watch.craft.Scraper.Output.ScrapedItem
import watch.craft.executor.ScraperAdapter
import java.net.URI

private const val GOLDEN_DATE = "2020-07-10"

fun executeScraper(scraper: Scraper, dateString: String? = GOLDEN_DATE) = runBlocking {
  validateNonLiveOnCi(dateString)
  val id = findBreweryId(scraper)
  ScraperAdapter(
    StorageStructure(dateString).createRetriever(id),
    scraper,
    id
  ).execute().entries.map { it.item }
}

private fun validateNonLiveOnCi(dateString: String?) {
  if (dateString == null && System.getenv("CI") != null) {
    throw RuntimeException("Shouldn't be running live tests on CI")
  }
}

private fun findBreweryId(scraper: Scraper) = SCRAPERS.find { it.scraper.javaClass == scraper.javaClass }
  ?.brewery?.id
  ?: throw RuntimeException("Can't find scraper entry")

fun List<ScrapedItem>.byName(name: String) = firstOrNull { it.name == name } ?: fail("No match for '${name}'")

fun ScrapedItem.noDesc() = copy(desc = null)    // Makes it easier to test item equality

fun ScrapedItem.onlyOffer() = offers.single()

fun List<ScrapedItem>.display() = forEach { println(it.noDesc()) }

val PROTOTYPE_ITEM = Item(
  breweryId = "",
  name = "",
  offers = listOf(Offer(quantity = 1, totalPrice = 0.00)),
  available = false,
  thumbnailUrl = URI(""),
  url = URI("")
)
