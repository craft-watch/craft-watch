package watch.craft.scrapers

import watch.craft.Offer
import watch.craft.Scraper
import watch.craft.Scraper.Job.Leaf
import watch.craft.Scraper.ScrapedItem
import watch.craft.dsl.*
import watch.craft.jsonld.Thing.Product
import watch.craft.jsonld.jsonLdFrom

class HowlingHopsScraper : Scraper {
  override val jobs = forRoots(ROOT) { root ->
    root
      .selectFrom(".wc-block-handpicked-products") // Avoid apparel
      .selectMultipleFrom(".wc-block-grid__product")
      .map { el ->
        val a = el.selectFrom(".wc-block-grid__product-link")
        val rawName = el.textFrom(".wc-block-grid__product-title")

        Leaf(rawName, a.urlFrom()) { doc ->
          val desc = doc.textFrom(".woocommerce-product-details__short-description")
          val parts = extractVariableParts(desc)
          val product = doc.jsonLdFrom<Product>().single()
          val available = product.offers.any { it.availability == "http://schema.org/InStock" }

          ScrapedItem(
            thumbnailUrl = a.urlFrom(".attachment-woocommerce_thumbnail"),
            name = parts.name,
            summary = parts.summary,
            desc = doc.maybe { formattedTextFrom(".woocommerce-product-details__short-description") },
            mixed = parts.mixed,
            available = available,
            abv = parts.abv,
            offers = setOf(
              Offer(
                quantity = parts.numCans,
                totalPrice = el.priceFrom(":not(del) > .woocommerce-Price-amount"),
                sizeMl = desc.sizeMlFrom()
              )
            )

          )
        }
      }
  }

  private data class VariableParts(
    val name: String,
    val summary: String? = null,
    val abv: Double? = null,
    val numCans: Int,
    val mixed: Boolean = false
  )

  private fun extractVariableParts(desc: String): VariableParts {
    val parts = desc.maybe { extract("([^/]*?) / ([^/]*?) / (\\d+) (?:.*?) / (\\d+(\\.\\d+)?)% ABV") }
    return if (parts != null) {
      VariableParts(
        name = parts[1],
        summary = parts[2],
        abv = parts[4].toDouble(),
        numCans = parts[3].toInt()
      )
    } else {
      val betterParts = desc.extract("(.*?) (\\d+) x")
      val numCans = betterParts[2].toInt()
      VariableParts(
        name = betterParts[1],
        numCans = numCans,
        mixed = true
      )
    }
  }

  companion object {
    private val ROOT = root("https://www.howlinghops.co.uk/shop")
  }
}
