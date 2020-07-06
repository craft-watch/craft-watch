package watch.craft.scrapers

import watch.craft.*
import watch.craft.Scraper.Job.Leaf
import watch.craft.Scraper.ScrapedItem
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
          if (doc.maybeSelectFrom(".stat") == null) {
            throw SkipItemException("Not an actual beer")
          }

          val rawSize = doc.textFrom(".size .stat")
          val sizeParts = rawSize.extract("(\\d+)\\s*(ml|L|litre(s?))")

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
            summary = doc.maybeTextFrom(".tag_line")?.replace(" \\d+(\\.\\d+)?%$".toRegex(), ""),
            desc = doc.textFrom(".post_content"),
            mixed = mixed,
            keg = rawName.contains("mini( ?)keg".toRegex(IGNORE_CASE)),
            abv = if (mixed) null else doc.extractFrom(".abv .stat", "\\d+(\\.\\d+)?")[0].toDouble(),
            sizeMl = if (mixed) null else sizeParts[1].toInt() * (if (sizeParts[2] in listOf("L", "litre", "litres")) 1000 else 1),
            available = true,
            numItems = rawSize.maybeExtract("(\\d+) x")?.get(1)?.toInt() ?: 1,
            price = el.priceFrom(".woocommerce-Price-amount")
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
