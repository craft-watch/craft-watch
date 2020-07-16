package watch.craft.scrapers

import watch.craft.Format.KEG
import watch.craft.Offer
import watch.craft.Scraper
import watch.craft.Scraper.Job.Leaf
import watch.craft.Scraper.ScrapedItem
import watch.craft.SkipItemException
import watch.craft.utils.*
import java.net.URI

class SirenScraper : Scraper {
  override val jobs = forRootUrls(ROOT_URL) { root ->
    root
      .selectMultipleFrom(".itemsBrowse .itemWrap")
      .map { el ->
        val itemName = el.selectFrom(".itemName")
        val rawName = itemName.text()

        Leaf(rawName, itemName.urlFrom("a")) { doc ->
          if (rawName.containsMatch("Mixed")) {
            throw SkipItemException("Can't deal with mixed cases yet")    // TODO
          }

          val detailsText = doc.textFrom(".itemTitle .small")
          if (detailsText.containsMatch("Mixed")) {
            throw SkipItemException("Can't deal with mixed cases yet")    // TODO
          }
          val details = detailsText.extract("(.*?)\\s+\\|\\s+(\\d+(\\.\\d+)?)%\\s+\\|\\s+(\\d+)")

          val keg = rawName.containsMatch("Mini Keg")

          ScrapedItem(
            name = rawName.remove("(\\d+)L Mini Keg - "),
            summary = if (keg) null else details[1],
            desc = doc.formattedTextFrom(".about"),
            mixed = false,
            abv = details[2].toDouble(),
            available = ".unavailableItemWrap" !in doc,
            offers = setOf(
              Offer(
                totalPrice = el.priceFrom(".itemPriceWrap"),
                format = if (keg) KEG else null,
                sizeMl = if (keg) 5000 else details[4].toInt()
              )
            ),
            thumbnailUrl = el.urlFrom(".imageInnerWrap img")
          )
        }
      }
  }

  companion object {
    private val ROOT_URL = URI("https://www.sirencraftbrew.com/browse/c-Beers-11")
  }
}
