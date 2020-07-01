package watch.craft.scrapers

import org.jsoup.nodes.Document
import watch.craft.*
import watch.craft.Scraper.IndexEntry
import watch.craft.Scraper.Item
import java.net.URI

class PillarsScraper : Scraper {
  override val name = "Pillars"
  override val rootUrls = listOf(URI("https://shop.pillarsbrewery.com/collections/pillars-beers"))

  override fun scrapeIndex(root: Document) = root
    .shopifyItems()
    .map { details ->
      IndexEntry(details.title, details.url) { doc ->
        val titleParts = extractTitleParts(details.title)
        val descParts = doc.maybeExtractFrom(
          ".product-single__description",
          "STYLE:\\s+(.+?)\\s+ABV:\\s+(\\d\\.\\d+)%"
        ) ?: throw SkipItemException("Couldn't find style or ABV")  // If we don't see these fields, assume we're not looking at a beer product

        Item(
          thumbnailUrl = details.thumbnailUrl,
          name = titleParts.name,
          summary = descParts[1].toTitleCase(),
          desc = doc.maybeWholeTextFrom(".product-single__description")?.extract("(.*?)STYLE:")?.get(1),
          keg = titleParts.keg,
          sizeMl = titleParts.sizeMl,
          abv = descParts[2].toDouble(),
          available = details.available,
          numItems = titleParts.numItems,
          price = details.price
        )
      }
    }

  private data class TitleParts(
    val name: String,
    val sizeMl: Int? = null,
    val numItems: Int = 1,
    val keg: Boolean = false
  )

  private fun extractTitleParts(title: String) = when {
    title.contains("Case") -> {
      val parts = title.extract("(.*?) Case of (\\d+)")
      TitleParts(name = parts[1], numItems = parts[2].toInt())
    }
    title.contains("Keg") -> {
      val parts = title.extract("(.*?) (\\d+)L")
      TitleParts(name = parts[1], sizeMl = parts[2].toInt() * 1000, keg = true)
    }
    else -> TitleParts(name = title)
  }
}
