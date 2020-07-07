package watch.craft.scrapers

import watch.craft.*
import watch.craft.Scraper.Job.Leaf
import watch.craft.Scraper.ScrapedItem
import watch.craft.utils.*
import java.net.URI

class GipsyHillScraper : Scraper {
  override val brewery = Brewery(
    shortName = "Gipsy Hill",
    name = "Gipsy Hill Brewing",
    location = "Gispy Hill, London",
    websiteUrl = URI("https://gipsyhillbrew.com/")
  )

  override val jobs = forRootUrls(ROOT_URL) { root ->
    root
      .selectMultipleFrom(".product")
      .map { el ->
        val a = el.selectFrom(".woocommerce-LoopProduct-link")
        val rawName = a.textFrom(".woocommerce-loop-product__title")

        Leaf(rawName, a.hrefFrom()) { doc ->
          val rawSummary = doc.textFrom(".summary")
          val numCans = doc.maybe { selectMultipleFrom(".woosb-title-inner") }
            ?.map { it.extractFrom(regex = "(\\d+) ×")[1].toInt() }?.sum()
            ?: 1
          val style = rawSummary.maybe { extract("Style: (.*) ABV") }?.get(1)
          val mixed = style in listOf("Various", "Mixed")

          val name = rawName.replace(" \\(.*\\)$".toRegex(), "")

          ScrapedItem(
            thumbnailUrl = a.srcFrom(".attachment-woocommerce_thumbnail"),
            name = name,
            summary = if (mixed) null else style,
            desc = doc.maybe { formattedTextFrom(".description") },
            mixed = mixed,
            available = true, // TODO
            abv = if (mixed) null else rawSummary.maybe { abvFrom(prefix = "ABV: ", optionalPercent = true) },
            sizeMl = rawSummary.maybe { sizeMlFrom() },
            numItems = numCans,
            price = el.priceFrom(".woocommerce-Price-amount")
          )
        }
      }
  }

  companion object {
    private val ROOT_URL = URI("https://gipsyhillbrew.com")
  }
}
