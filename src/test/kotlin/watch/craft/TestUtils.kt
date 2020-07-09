package watch.craft

import kotlinx.coroutines.runBlocking
import watch.craft.Scraper.ScrapedItem
import watch.craft.executor.ScraperAdapter
import java.net.URI

private const val GOLDEN_DATE = "2020-07-03"

fun executeScraper(scraper: Scraper, dateString: String? = GOLDEN_DATE) = runBlocking {
  ScraperAdapter(
    Setup(dateString).createRetriever(scraper.brewery.shortName),
    scraper
  ).execute().map { it.item }
}

fun List<ScrapedItem>.byName(name: String) = first { it.name == name }

fun ScrapedItem.noDesc() = copy(desc = null)    // Makes it easier to test item equality

fun List<ScrapedItem>.display() = forEach { println(it.noDesc()) }

val PROTOTYPE_ITEM = Item(
  brewery = "",
  name = "",
  offers = setOf(Offer(quantity = 1, totalPrice = 0.00)),
  available = false,
  thumbnailUrl = URI(""),
  url = URI("")
)
