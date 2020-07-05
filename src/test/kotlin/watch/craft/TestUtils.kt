package watch.craft

import watch.craft.Scraper.ScrapedItem
import watch.craft.executor.ConcurrentRawScraperExecutor

private const val GOLDEN_DATE = "2020-07-03"

fun executeScraper(scraper: Scraper, dateString: String? = GOLDEN_DATE) =
  ConcurrentRawScraperExecutor(4, Setup(dateString).getter)
    .execute(listOf(scraper))
    .map { it.item }

fun List<ScrapedItem>.byName(name: String) = first { it.name == name }

fun ScrapedItem.noDesc() = copy(desc = null)    // Makes it easier to test item equality

val PROTOTYPE_ITEM = Item(
  brewery = "",
  name = "",
  perItemPrice = 0.00,
  available = false,
  thumbnailUrl = "",
  url = ""
)
