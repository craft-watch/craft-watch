package choliver.neapi.scrapers

import choliver.neapi.*
import choliver.neapi.Scraper.IndexEntry
import choliver.neapi.Scraper.Result.Item
import choliver.neapi.Scraper.Result.Skipped
import org.jsoup.nodes.Document
import java.net.URI

class FivePointsScraper : Scraper {
  override val name = "Five Points"
  override val rootUrl = URI("https://shop.fivepointsbrewing.co.uk/browse/c-Beers-11")

  // TODO - barley wine
  // TODO - kegs

  override fun scrapeIndex(root: Document) = root
    .selectMultipleFrom("#browse li .itemWrap")
    .map { el ->
      val a = el.selectFrom("h2 a")

      IndexEntry(a.hrefFrom()) { doc ->
        val parts = doc.maybeSelectFrom(".itemTitle .small")
          ?.extractFrom(regex = "(.*?)\\s+\\|\\s+(\\d+(\\.\\d+)?)%\\s+\\|\\s+((\\d+)\\s+x\\s+)?(\\d+)ml")

        if (parts == null) {
          Skipped("Could not extract details")
        } else {
          val numCans = parts[5].ifBlank { "1" }.toInt()
          Item(
            thumbnailUrl = el.srcFrom(".imageInnerWrap img"),
            name = a.extractFrom(regex = "([^(]+)")!![1].trim().toTitleCase(),
            summary = parts[1],
            abv = parts[2].toDouble(),
            sizeMl = parts[6].toInt(),
            available = doc.maybeSelectFrom(".unavailableItemWrap") == null,
            perItemPrice = el.extractFrom(".priceStandard", "£(\\d+\\.\\d+)")!![1].toDouble()
              .divideAsPrice(numCans)
          )
        }
      }
    }

  private fun removeSizeSuffix(str: String) = if (str.endsWith("ml")) {
    str.extract(regex = "^(.+?)( \\d+ml)")!![1]
  } else {
    str
  }
}
