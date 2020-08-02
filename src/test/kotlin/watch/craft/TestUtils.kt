package watch.craft

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.fail
import watch.craft.Scraper.Node.ScrapedItem
import watch.craft.executor.ScraperAdapter
import java.net.URI

private const val GOLDEN_DATE = "2020-07-10"

fun executeScraper(scraper: Scraper, dateString: String? = GOLDEN_DATE) = runBlocking {
  validateNonLiveOnCi(dateString)
  val id = findBreweryId(scraper)
  ScraperAdapter(
    StorageStructure(dateString).createRetriever(id, scraper.failOn404),
    scraper,
    id
  ).execute().entries.map { it.item }
}

private fun findBreweryId(scraper: Scraper) = SCRAPERS.find { it.scraper.javaClass == scraper.javaClass }
  ?.brewery?.id
  ?: throw RuntimeException("Can't find scraper entry")

fun List<ScrapedItem>.byName(name: String) = firstOrNull { it.name == name } ?: fail("No match for '${name}'")

fun ScrapedItem.noDesc() = copy(desc = null)    // Makes it easier to test item equality

fun ScrapedItem.onlyOffer() = offers.single()

fun List<ScrapedItem>.display() {
  if (runningOnCi) {
    throw RuntimeException("Shouldn't be displaying on CI")
  }
  forEach { println(it.noDesc()) }
}

private fun validateNonLiveOnCi(dateString: String?) {
  if (dateString == null && runningOnCi) {
    throw RuntimeException("Shouldn't be running live tests on CI")
  }
}

private val runningOnCi = System.getenv("CI") != null

val PROTOTYPE_ITEM = Item(
  breweryId = "",
  name = "",
  offers = listOf(Offer(quantity = 1, totalPrice = 0.00)),
  available = false,
  thumbnailUrl = URI(""),
  url = URI("")
)
