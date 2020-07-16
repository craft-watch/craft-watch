package watch.craft.scrapers

import watch.craft.Format
import watch.craft.Offer
import watch.craft.Scraper
import watch.craft.Scraper.Job.Leaf
import watch.craft.Scraper.ScrapedItem
import watch.craft.SkipItemException
import watch.craft.utils.*
import java.net.URI

class FivePointsScraper : Scraper {
  override val jobs = forRootUrls(ROOT_URL) { root ->
    root
      .selectMultipleFrom("#browse li .itemWrap")
      .map { el ->
        val a = el.selectFrom("h2 a")

        Leaf(a.text(), a.urlFrom()) { doc ->
          val title = doc.maybe { selectFrom(".itemTitle .small") }
          val parts = title?.maybe {
            extractFrom(regex = "(.*?)\\s+\\|\\s+(\\d+(\\.\\d+)?)%\\s+\\|\\s+((\\d+)\\s+x\\s+)?")
          } ?: throw SkipItemException("Could not extract details")

          val sizeMl = title.sizeMlFrom()
          ScrapedItem(
            name = a.extractFrom(regex = "([^(]+)").stringFrom(1).toTitleCase(),
            summary = parts[1],
            desc = doc.formattedTextFrom(".about"),
            abv = parts[2].toDouble(),
            available = ".unavailableItemWrap" !in doc,
            offers = setOf(
              Offer(
                quantity = parts[5].ifBlank { "1" }.toInt(),
                totalPrice = el.priceFrom(".priceStandard"),
                sizeMl = sizeMl,
                format = if (sizeMl >= 1000) Format.KEG else null
              )
            ),
            thumbnailUrl = el.urlFrom(".imageInnerWrap img")
          )
        }
      }
  }

  companion object {
    private val ROOT_URL = URI("https://shop.fivepointsbrewing.co.uk/browse/c-Beers-11")
  }
}
