package watch.craft

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.fail
import watch.craft.Scraper.ScrapedItem
import watch.craft.executor.ScraperAdapter
import java.net.URI

private const val GOLDEN_DATE = "2020-07-10"

fun executeScraper(scraper: Scraper, dateString: String? = GOLDEN_DATE) = runBlocking {
  ScraperAdapter(
    Setup(dateString).createRetriever("TEST"),
    scraper,
    "TEST"
  ).execute().entries.map { it.item }
}

fun List<ScrapedItem>.byName(name: String) = firstOrNull { it.name == name } ?: fail("No match for '${name}'")

fun ScrapedItem.noDesc() = copy(desc = null)    // Makes it easier to test item equality

fun ScrapedItem.onlyOffer() = offers.single()

fun List<ScrapedItem>.display() = forEach { println(it.noDesc()) }

val PROTOTYPE_ITEM = Item(
  brewery = "",
  name = "",
  offers = listOf(Offer(quantity = 1, totalPrice = 0.00)),
  available = false,
  thumbnailUrl = URI(""),
  url = URI("")
)
