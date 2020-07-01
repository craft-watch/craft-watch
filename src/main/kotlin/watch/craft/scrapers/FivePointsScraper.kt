package watch.craft.scrapers

import org.jsoup.nodes.Document
import watch.craft.*
import watch.craft.Scraper.IndexEntry
import watch.craft.Scraper.Item
import java.net.URI

class FivePointsScraper : Scraper {
  override val name = "Five Points"
  override val rootUrls = listOf(URI("https://shop.fivepointsbrewing.co.uk/browse/c-Beers-11"))

  override fun scrapeIndex(root: Document) = root
    .selectMultipleFrom("#browse li .itemWrap")
    .map { el ->
      val a = el.selectFrom("h2 a")

      IndexEntry(a.text(), a.hrefFrom()) { doc ->
        val parts = doc.maybeExtractFrom(
          ".itemTitle .small",
          "(.*?)\\s+\\|\\s+(\\d+(\\.\\d+)?)%\\s+\\|\\s+((\\d+)\\s+x\\s+)?(\\d+)(ml|L)"
        ) ?: throw SkipItemException("Could not extract details")

        val sizeMl = parts[6].toInt() * (if (parts[7] == "L") 1000 else 1)
        Item(
          thumbnailUrl = el.srcFrom(".imageInnerWrap img"),
          name = a.extractFrom(regex = "([^(]+)")[1].trim().toTitleCase(),
          summary = parts[1],
          desc = doc.maybeSelectMultipleFrom(".about p").joinToString("\n") { it.text() },
          keg = (sizeMl >= 1000),
          abv = parts[2].toDouble(),
          sizeMl = sizeMl,
          available = doc.maybeSelectFrom(".unavailableItemWrap") == null,
          numItems = parts[5].ifBlank { "1" }.toInt(),
          price = el.extractFrom(".priceStandard", "£(\\d+\\.\\d+)")[1].toDouble()
        )
      }
    }
}
