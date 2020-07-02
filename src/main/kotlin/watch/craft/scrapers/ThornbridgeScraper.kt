package watch.craft.scrapers

import org.jsoup.nodes.Document
import watch.craft.*
import watch.craft.Scraper.IndexEntry
import watch.craft.Scraper.ScrapedItem
import java.net.URI
import kotlin.text.RegexOption.IGNORE_CASE

class ThornbridgeScraper : Scraper {
  override val name = "Thornbridge"
  // TODO - URI("https://shop.thornbridgebrewery.co.uk/collections/smart-collection?view=list")
  override val rootUrls = listOf(
    URI("https://shop.thornbridgebrewery.co.uk/collections/pick-and-mix-beers?view=list")
  )

  override fun scrapeIndex(root: Document) = root
    .selectMultipleFrom(".grid-uniform > .grid-item")
    .map { el ->
      val rawName = el.textFrom(".h6")

      IndexEntry(rawName, el.hrefFrom("a")) { doc ->
        if (!rawName.contains("%")) {
          throw SkipItemException("No ABV in title, so assume it's not a beer")
        }

        val parts = rawName.extract("(.*?)\\W+(\\d(\\.\\d+)?)%\\W+(.*)")
        val desc = doc.selectFrom(".product-description")

        // TODO - identify mixed packs

        ScrapedItem(
          thumbnailUrl = doc.srcFrom(".product__image-wrapper img"),
          name = parts[1].replace(" (bottle|can)$".toRegex(IGNORE_CASE), ""),
          summary = parts[4],
          desc = desc.selectMultipleFrom("p").joinToString("\n") { it.text() },
          mixed = false,
          sizeMl = desc.maybeExtractFrom(regex = "(\\d+)ml")?.get(1)?.toInt(),
          abv = parts[2].toDouble(),
          available = "sold-out" !in el.classNames(),
          numItems = desc.maybeExtractFrom(regex = "(\\d+)\\s*x")?.get(1)?.toInt()
            ?: rawName.maybeExtract(regex = "(\\d+)\\s*x")?.get(1)?.toInt()
            ?: 1,
          price = el.priceFrom(".product-item--price")
        )
      }
    }
}
