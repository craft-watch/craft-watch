package watch.craft.scrapers

import watch.craft.Format.KEG
import watch.craft.Offer
import watch.craft.Scraper
import watch.craft.Scraper.Job.Leaf
import watch.craft.Scraper.ScrapedItem
import watch.craft.SkipItemException
import watch.craft.utils.*
import java.net.URI

class PadstowScraper : Scraper {
  override val jobs = forRootUrls(*ROOT_URLS) { root ->
    root
      .selectMultipleFrom(".beer-container")
      .map { el ->
        val rawName = el.textFrom("h3")

        Leaf(rawName, el.urlFrom(".woocommerce-LoopProduct-link")) { doc ->
          if (".stat" !in doc) {
            throw SkipItemException("Not an actual beer")
          }

          val rawSize = doc.textFrom(".size .stat")

          val name = rawName.remove("^.* –", "\\(.*\\)", "\\d+-pack", "mini( ?)keg")

          val mixed = doc.containsMatchFrom(".style .stat", "mixed")

          ScrapedItem(
            thumbnailUrl = el.urlFrom(".attachment-woocommerce_thumbnail"),
            name = name,
            summary = doc.maybe { textFrom(".tag_line") }?.remove(" \\d+(\\.\\d+)?%$"),
            desc = doc.textFrom(".post_content"),
            mixed = mixed,
            abv = if (mixed) null else doc.extractFrom(".abv .stat", "\\d+(\\.\\d+)?").doubleFrom(0),
            available = true,
            offers = setOf(
              Offer(
                quantity = rawSize.maybe { quantityFrom() } ?: 1,
                totalPrice = el.priceFrom(".woocommerce-Price-amount"),
                format = if (rawName.containsMatch("mini( ?)keg")) KEG else null,
                sizeMl = if (mixed) null else rawSize.sizeMlFrom()
              )
            )
          )
        }
      }
  }

  companion object {
    private val ROOT_URLS = arrayOf(
      URI("https://www.padstowbrewing.co.uk/shop/beers/"),
      URI("https://www.padstowbrewing.co.uk/shop/ciders/"),
      URI("https://www.padstowbrewing.co.uk/shop/kegs-cases/")
    )
  }
}
