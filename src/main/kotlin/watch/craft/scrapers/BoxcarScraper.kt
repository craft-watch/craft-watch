package watch.craft.scrapers

import org.jsoup.nodes.Document
import watch.craft.*
import watch.craft.Scraper.IndexEntry
import watch.craft.Scraper.ScrapedItem
import java.net.URI
import kotlin.text.RegexOption.IGNORE_CASE

class BoxcarScraper : Scraper {
  override val brewery = Brewery(
    shortName = "Boxcar",
    name = "Boxcar Brewery",
    location = "Bethnal Green, London",
    websiteUrl = URI("https://boxcarbrewery.co.uk/")
  )
  override val rootUrls = listOf(URI("https://shop.boxcarbrewery.co.uk/collections/beer"))

  override fun scrapeIndex(root: Document) = root
    .shopifyItems()
    .map { details ->
      IndexEntry(details.title, details.url) { doc ->
        val parts = details.title.extract("^(.*?) // (.*?)% *(.*?)? // (.*?)ml$")

        ScrapedItem(
          thumbnailUrl = details.thumbnailUrl,
          name = parts[1],
          abv = parts[2].toDouble(),
          summary = parts[3].ifBlank { null },
          desc = doc.maybeWholeTextFrom(".product-single__description")
            ?.replace("^DESCRIPTION".toRegex(IGNORE_CASE), ""),
          sizeMl = parts[4].toInt(),
          available = details.available,
          price = details.price
        )
      }
    }
}
