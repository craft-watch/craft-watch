package choliver.neapi.scrapers

import choliver.neapi.*
import choliver.neapi.Scraper.IndexEntry
import org.jsoup.nodes.Document
import java.net.URI

class PillarsScraper : Scraper {
  override val name = "Pillars"
  override val rootUrl = URI("https://shop.pillarsbrewery.com/collections/pillars-beers")

  override fun scrapeIndex(root: Document) = root
    .shopifyItems()
    .map { details ->
      IndexEntry(details.url) { doc ->
        val titleParts = extractTitleParts(details.title)
        val descParts = doc.extractFrom(
          ".product-single__description",
          "STYLE:\\s+(.+?)\\s+ABV:\\s+(\\d\\.\\d+)%"
        )

        // If we don't see these fields, assume we're not looking at a beer product
        if (descParts == null) {
          null
        } else {
          ScrapedItem(
            thumbnailUrl = details.thumbnailUrl,
            name = titleParts.name,
            summary = if (titleParts.keg) "Minikeg" else descParts[1].toTitleCase(),
            sizeMl = titleParts.sizeMl,
            abv = descParts[2].toDouble(),
            available = details.available,
            perItemPrice = details.price.divideAsPrice(titleParts.numItems)
          )
        }
      }
    }
    // TODO
    //.bestPricedItems()

  private data class TitleParts(
    val name: String,
    val sizeMl: Int? = null,
    val numItems: Int = 1,
    val keg: Boolean = false
  )

  private fun extractTitleParts(title: String) = when {
    title.contains("Case") -> {
      val parts = title.extract("(.*?) Case of (\\d+)")!!
      TitleParts(name = parts[1], numItems = parts[2].toInt())
    }
    title.contains("Keg") -> {
      val parts = title.extract("(.*?) (\\d+)L")!!
      TitleParts(name = parts[1], sizeMl = parts[2].toInt() * 1000, keg = true)
    }
    else -> TitleParts(name = title)
  }
}
