package watch.craft

import kotlinx.coroutines.runBlocking
import watch.craft.Scraper.ScrapedItem
import watch.craft.executor.ScraperAdapter
import java.net.URI

private const val GOLDEN_DATE = "2020-07-03"

fun executeScraper(scraper: Scraper, dateString: String? = GOLDEN_DATE) = runBlocking {
  ScraperAdapter(Setup(dateString).retriever, scraper).execute().map { it.item }
}

fun List<ScrapedItem>.byName(name: String) = first { it.name == name }

fun ScrapedItem.noDesc() = copy(desc = null)    // Makes it easier to test item equality

val PROTOTYPE_ITEM = Item(
  brewery = "",
  name = "",
  perItemPrice = 0.00,
  available = false,
  thumbnailUrl = URI(""),
  url = URI("")
)
