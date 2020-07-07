package watch.craft.scrapers

import watch.craft.*
import watch.craft.Scraper.Job.Leaf
import watch.craft.Scraper.ScrapedItem
import watch.craft.utils.*
import java.net.URI

class SirenScraper : Scraper {
  override val brewery = Brewery(
    shortName = "Siren Craft",
    name = "Siren Craft Brew",
    location = "Finchampstead, Berkshire",
    websiteUrl = URI("https://www.sirencraftbrew.com/")
  )

  override val jobs = forRootUrls(ROOT_URL) { root ->
    root
      .selectMultipleFrom(".itemsBrowse .itemWrap")
      .map { el ->
        val itemName = el.selectFrom(".itemName")
        val rawName = itemName.text()

        Leaf(rawName, itemName.hrefFrom("a")) { doc ->
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
            desc = doc.formattedTextFrom(".about"),
            keg = keg,
            mixed = false,
            sizeMl = if (keg) 5000 else details[4].toInt(),
            abv = details[2].toDouble(),
            available = ".unavailableItemWrap" !in doc,
            numItems = 1,
            price = el.priceFrom(".itemPriceWrap"),
            thumbnailUrl = el.srcFrom(".imageInnerWrap img")
          )
        }
      }
  }

  companion object {
    private val ROOT_URL = URI("https://www.sirencraftbrew.com/browse/c-Beers-11")
  }
}
