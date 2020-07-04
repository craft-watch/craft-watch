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
        if (rawName.contains("Mixed", ignoreCase = true)) {
          throw SkipItemException("Can't deal with mixed cases yet")    // TODO
        }

        val detailsText = doc.textFrom(".itemTitle .small")
        if (detailsText.contains("Mixed", ignoreCase = true)) {
          throw SkipItemException("Can't deal with mixed cases yet")    // TODO
        }
        val details = detailsText.extract("(.*?)\\s+\\|\\s+(\\d+(\\.\\d+)?)%\\s+\\|\\s+(\\d+)")

        val keg = rawName.contains("Mini Keg", ignoreCase = true)

        ScrapedItem(
          name = rawName.replace("(\\d+)L Mini Keg - ".toRegex(), ""),
          summary = if (keg) null else details[1],
          desc = doc.normaliseParagraphsFrom(".about"),
          keg = keg,
          mixed = false,
          sizeMl = if (keg) 5000 else details[4].toInt(),
          abv = details[2].toDouble(),
          available = true,
          numItems = 1,
          price = doc.priceFrom(".priceNow"),
          thumbnailUrl = el.srcFrom(".imageInnerWrap img")
        )
      }
    }
}
