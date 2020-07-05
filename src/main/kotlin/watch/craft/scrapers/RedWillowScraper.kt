package watch.craft.scrapers

import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import watch.craft.*
import watch.craft.Scraper.IndexEntry
import watch.craft.Scraper.ScrapedItem
import java.net.URI

class RedWillowScraper : Scraper {
  override val name = "Red Willow"
  override val rootUrls = listOf(URI("https://www.redwillowbrewery.com/shop"))

  override fun scrapeIndex(root: Document) = root
    .selectMultipleFrom(".ProductList-grid .ProductList-item")
    .map { el ->
      val rawName = el.textFrom(".ProductList-title")

      IndexEntry(rawName, el.hrefFrom("a.ProductList-item-link")) { doc ->

        ScrapedItem(
          name = rawName,
          summary = null,
          desc = null,
          sizeMl = null,
          abv = null,
          available = true,
          numItems = 1,
          price = 0.00,
          thumbnailUrl = el.srcFrom("img.ProductList-image")
        )
      }
    }

}
