package watch.craft.scrapers

import org.jsoup.nodes.Document
import watch.craft.*
import watch.craft.Scraper.IndexEntry
import watch.craft.Scraper.ScrapedItem
import java.net.URI

class SirenScraper : Scraper {
  override val name = "Siren Craft"
  override val rootUrls = listOf(URI("https://www.sirencraftbrew.com/browse/c-Beers-11"))

  override fun scrapeIndex(root: Document) = root
    .selectMultipleFrom(".itemsBrowse .itemWrap")
    .map { el ->
      val itemName = el.selectFrom(".itemName")
      val rawName = itemName.text()

      IndexEntry(rawName, itemName.hrefFrom("a")) { doc ->
        val keg = rawName.contains("Mini Keg", ignoreCase = true)

        val details = doc.extractFrom(".itemTitle .small", "(.*?)\\s+\\|\\s+(\\d+(\\.\\d+)?)%\\s+\\|\\s+(\\d+)")

        println(details)


        ScrapedItem(
          name = rawName,
          summary = details[1],
          desc = null,    // doc.normaliseParagraphsFrom(".about")
          keg = keg,
          mixed = false,
          sizeMl = details[4].toInt(),
          abv = details[2].toDouble(),
          available = true,
          numItems = 1,
          price = doc.priceFrom(".priceNow"),
          thumbnailUrl = el.srcFrom(".imageInnerWrap img")
        )
      }
    }
}
