package watch.craft.scrapers

import watch.craft.*
import watch.craft.Format.KEG
import watch.craft.Scraper.Job.Leaf
import watch.craft.Scraper.ScrapedItem
import watch.craft.utils.*
import java.net.URI
import kotlin.text.RegexOption.IGNORE_CASE

class PadstowScraper : Scraper {
  override val brewery = Brewery(
    shortName = "Padstow",
    name = "Padstow Brewing Co",
    location = "Padstow, Cornwall",
    websiteUrl = URI("https://www.padstowbrewing.co.uk/")
  )

  override val jobs = forRootUrls(*ROOT_URLS) { root ->
    root
      .selectMultipleFrom(".beer-container")
      .map { el ->
        val rawName = el.textFrom("h3")

        Leaf(rawName, el.hrefFrom(".woocommerce-LoopProduct-link")) { doc ->
          if (".stat" !in doc) {
            throw SkipItemException("Not an actual beer")
          }

          val rawSize = doc.textFrom(".size .stat")

          val name = rawName
            .replace("^.* –".toRegex(), "")
            .replace("\\(.*\\)".toRegex(), "")
            .replace("\\d+-pack".toRegex(IGNORE_CASE), "")
            .replace("mini( ?)keg".toRegex(IGNORE_CASE), "")
            .trim()

          val mixed = doc.textFrom(".style .stat").contains("mixed", ignoreCase = true)

          ScrapedItem(
            thumbnailUrl = el.srcFrom(".attachment-woocommerce_thumbnail"),
            name = name,
            summary = doc.maybe { textFrom(".tag_line") }?.replace(" \\d+(\\.\\d+)?%$".toRegex(), ""),
            desc = doc.textFrom(".post_content"),
            mixed = mixed,
            abv = if (mixed) null else doc.extractFrom(".abv .stat", "\\d+(\\.\\d+)?").doubleFrom(0),
            available = true,
            offers = setOf(
              Offer(
                quantity = rawSize.maybe { extract("(\\d+) x").intFrom(1) } ?: 1,
                totalPrice = el.priceFrom(".woocommerce-Price-amount"),
                format = if (rawName.contains("mini( ?)keg".toRegex(IGNORE_CASE))) KEG else null,
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
