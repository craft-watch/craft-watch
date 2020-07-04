package watch.craft.scrapers

import org.jsoup.nodes.Document
import watch.craft.*
import watch.craft.Scraper.IndexEntry
import watch.craft.Scraper.ScrapedItem
import java.net.URI

class WanderScraper : Scraper {
  override val name = "Wander Beyond"
  override val rootUrls = listOf(URI("https://www.wanderbeyondbrewing.com/shop"))

  override fun scrapeIndex(root: Document) = root
    .selectFrom("product-list-wrapper".hook())  // Only first one, to avoid merch, etc.
    .selectMultipleFrom("product-list-grid-item".hook())
    .map { el ->
      val name = el.textFrom("product-item-name".hook())

      IndexEntry(name, el.hrefFrom("a")) { doc ->
        ScrapedItem(
          name = name,
          summary = null,
          desc = null,
          mixed = false,
          sizeMl = null,
          abv = null,
          available = true,
          numItems = 1,
          price = 0.00,
          thumbnailUrl = el.srcFrom("img")
        )
      }
    }

  private fun String.hook() = "[data-hook=${this}]"
}
