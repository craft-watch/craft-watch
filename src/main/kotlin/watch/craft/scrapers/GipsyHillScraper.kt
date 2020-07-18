package watch.craft.scrapers

import watch.craft.Offer
import watch.craft.Scraper
import watch.craft.Scraper.Job.Leaf
import watch.craft.Scraper.ScrapedItem
import watch.craft.dsl.*
import java.net.URI

class GipsyHillScraper : Scraper {
  override val jobs = forRootUrls(ROOT_URL) { root ->
    root
      .selectMultipleFrom(".product")
      .map { el ->
        val a = el.selectFrom(".woocommerce-LoopProduct-link")
        val rawName = a.textFrom(".woocommerce-loop-product__title")

        Leaf(rawName, a.urlFrom()) { doc ->
          val rawSummary = doc.textFrom(".summary")
          val numCans = doc.maybe { selectMultipleFrom(".woosb-title-inner") }
            ?.map { it.quantityFrom() }?.sum()
            ?: 1
          val style = rawSummary.maybe { extract("Style: (.*) ABV")[1] }
          val mixed = style in listOf("Various", "Mixed")

          val name = rawName.cleanse(" \\(.*\\)$")

          ScrapedItem(

            name = name,
            summary = if (mixed) null else style,
            desc = doc.maybe { formattedTextFrom(".description") },
            mixed = mixed,
            available = true, // TODO
            abv = if (mixed) null else rawSummary.maybe { abvFrom(prefix = "ABV: ", noPercent = true) },
            offers = setOf(
              Offer(
                quantity = numCans,
                totalPrice = el.priceFrom(".woocommerce-Price-amount"),
                sizeMl = rawSummary.maybe { sizeMlFrom() }
              )
            ),
            thumbnailUrl = a.urlFrom(".attachment-woocommerce_thumbnail")
          )
        }
      }
  }

  companion object {
    private val ROOT_URL = URI("https://gipsyhillbrew.com")
  }
}
