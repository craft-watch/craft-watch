package watch.craft.scrapers

import org.jsoup.nodes.Document
import watch.craft.Scraper
import watch.craft.Scraper.IndexEntry
import watch.craft.Scraper.ScrapedItem
import watch.craft.extract
import watch.craft.shopifyItems
import watch.craft.textFrom
import java.net.URI

class UnityScraper : Scraper {
  override val name = "Unity"
  override val rootUrls = listOf(URI("https://unitybrewingco.com/collections/unity-beer"))

  override fun scrapeIndex(root: Document) = root
    .shopifyItems()
    .map { details ->

      IndexEntry(details.title, details.url) { doc ->
        val desc = doc.textFrom(".product-single__description")
        val parts = desc.extract("(\\d+)ml / (\\d(\\.\\d+)?)%")

        ScrapedItem(
          name = details.title,
          summary = null,
          desc = desc,
          mixed = false,
          sizeMl = parts[1].toInt(),
          abv = parts[2].toDouble(),
          available = details.available,
          numItems = 1,
          price = details.price,
          thumbnailUrl = details.thumbnailUrl
        )
      }
    }
}
