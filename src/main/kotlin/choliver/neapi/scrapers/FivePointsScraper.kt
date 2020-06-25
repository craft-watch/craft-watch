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

  override fun scrapeIndex(root: Document) = root
    .selectMultipleFrom("#browse li .itemWrap")
    .map { el ->
      val a = el.selectFrom("h2 a")

      IndexEntry(a.hrefFrom()) { doc ->
        val parts = doc.maybeSelectFrom(".itemTitle .small")
          ?.extractFrom(regex = "(.*?)\\s+\\|\\s+(\\d+(\\.\\d+)?)%\\s+\\|\\s+((\\d+)\\s+x\\s+)?(\\d+)(ml|L)")

        if (parts == null) {
          Skipped("Could not extract details")
        } else {
          val numCans = parts[5].ifBlank { "1" }.toInt()
          val sizeMl = parts[6].toInt() * (if (parts[7] == "L") 1000 else 1)
          Item(
            thumbnailUrl = el.srcFrom(".imageInnerWrap img"),
            name = a.extractFrom(regex = "([^(]+)")!![1].trim().toTitleCase(),
            summary = if (sizeMl > 1000) "Minikeg" else parts[1],
            abv = parts[2].toDouble(),
            sizeMl = sizeMl,
            available = doc.maybeSelectFrom(".unavailableItemWrap") == null,
            perItemPrice = el.extractFrom(".priceStandard", "£(\\d+\\.\\d+)")!![1].toDouble()
              .divideAsPrice(numCans)
          )
        }
      }
    }
}
