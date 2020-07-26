package watch.craft.scrapers

import watch.craft.Offer
import watch.craft.Scraper

import watch.craft.Scraper.Node.ScrapedItem
import watch.craft.dsl.*

class GipsyHillScraper : Scraper {
  override val roots = fromHtmlRoots(ROOT) { root ->
    root()
      .selectMultipleFrom(".product")
      .map { el ->
        val a = el.selectFrom(".woocommerce-LoopProduct-link")
        val rawName = a.textFrom(".woocommerce-loop-product__title")

        fromHtml(rawName, a.urlFrom()) { doc ->
          val rawSummary = doc().textFrom(".summary")
          val numCans = doc().maybe { selectMultipleFrom(".woosb-title-inner") }
            ?.map { it.quantityFrom() }?.sum()
            ?: 1
          val style = rawSummary.maybe { extract("Style: (.*) ABV")[1] }
          val mixed = style in listOf("Various", "Mixed")

          val name = rawName.cleanse(" \\(.*\\)$")

          ScrapedItem(
            name = name,
            summary = if (mixed) null else style,
            desc = doc().maybe { formattedTextFrom(".description") },
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
    private val ROOT = root("https://gipsyhillbrew.com")
  }
}
