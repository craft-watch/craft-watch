package watch.craft.scrapers

import watch.craft.Format.KEG
import watch.craft.Offer
import watch.craft.Scraper

import watch.craft.Scraper.Node.ScrapedItem
import watch.craft.SkipItemException
import watch.craft.dsl.*

class SirenScraper : Scraper {
  override val roots = fromHtmlRoots(ROOT) { root ->
    root()
      .selectMultipleFrom(".itemsBrowse .itemWrap")
      .map { el ->
        val itemName = el.selectFrom(".itemName")
        val rawName = itemName.text()

        fromHtml(rawName, itemName.urlFrom("a")) { doc ->
          if (rawName.containsMatch("Mixed")) {
            throw SkipItemException("Can't deal with mixed cases yet")    // TODO
          }

          val details = doc().textFrom(".itemTitle .small")
          if (details.containsMatch("Mixed")) {
            throw SkipItemException("Can't deal with mixed cases yet")    // TODO
          }

          val keg = rawName.containsMatch("Mini Keg")

          ScrapedItem(
            name = rawName.cleanse("(\\d+)L Mini Keg - "),
            summary = if (keg) null else details.split(" | ")[0],
            desc = doc().formattedTextFrom(".about"),
            mixed = false,
            abv = details.abvFrom(),
            available = ".unavailableItemWrap" !in doc(),
            offers = setOf(
              Offer(
                totalPrice = el.priceFrom(".itemPriceWrap"),
                format = if (keg) KEG else null,
                sizeMl = ("$details ml").maybe { sizeMlFrom() },  // Size at end doesn't always have unit
                quantity = details.maybe { quantityFrom() } ?: 1
              )
            ),
            thumbnailUrl = el.urlFrom(".imageInnerWrap img")
          )
        }
      }
  }

  companion object {
    private val ROOT = root("https://www.sirencraftbrew.com/browse/c-Beers-11")
  }
}
