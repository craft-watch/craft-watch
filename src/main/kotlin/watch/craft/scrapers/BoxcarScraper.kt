package watch.craft.scrapers

import org.jsoup.nodes.Document
import watch.craft.Scraper
import watch.craft.Scraper.IndexEntry
import watch.craft.Scraper.Item
import watch.craft.extract
import watch.craft.maybeWholeTextFrom
import watch.craft.shopifyItems
import java.net.URI
import kotlin.text.RegexOption.IGNORE_CASE

class BoxcarScraper : Scraper {
  override val name = "Boxcar"
  override val rootUrls = listOf(URI("https://shop.boxcarbrewery.co.uk/collections/beer"))

  override fun scrapeIndex(root: Document) = root
    .shopifyItems()
    .map { details ->
      IndexEntry(details.title, details.url) { doc ->
        val parts = details.title.extract("^(.*?) // (.*?)% *(.*?)? // (.*?)ml$")

        Item(
          thumbnailUrl = details.thumbnailUrl,
          name = parts[1],
          abv = parts[2].toDouble(),
          summary = parts[3].ifBlank { null },
          desc = doc.maybeWholeTextFrom(".product-single__description")
            ?.replace("^DESCRIPTION".toRegex(IGNORE_CASE), ""),
          sizeMl = parts[4].toInt(),
          available = details.available,
          perItemPrice = details.price
        )
      }
    }
}
