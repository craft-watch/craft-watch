package watch.craft.scrapers

import watch.craft.Format
import watch.craft.Offer
import watch.craft.Scraper

import watch.craft.Scraper.Node.ScrapedItem
import watch.craft.dsl.*
import watch.craft.jsonld.Thing.Product
import watch.craft.jsonld.jsonLdFrom

class HowlingHopsScraper : Scraper {
  override val roots = fromHtmlRoots(ROOT) { root ->
    root()
      .selectFrom(".wc-block-handpicked-products") // Avoid apparel
      .selectMultipleFrom(".wc-block-grid__product")
      .map { el ->
        val a = el.selectFrom(".wc-block-grid__product-link")
        val title = el.textFrom(".wc-block-grid__product-title")

        fromHtml(title, a.urlFrom()) { doc ->
          val desc = doc().textFrom(".woocommerce-product-details__short-description")
          val parts = extractVariableParts(title, desc)
          val product = doc().jsonLdFrom<Product>().single()
          val available = product.offers.any { it.availability == "http://schema.org/InStock" }

          ScrapedItem(
            thumbnailUrl = a.urlFrom(".attachment-woocommerce_thumbnail"),
            name = parts.name,
            summary = parts.summary,
            desc = doc().maybe { formattedTextFrom(".woocommerce-product-details__short-description") },
            mixed = parts.mixed,
            available = available,
            abv = if (parts.mixed) null else desc.abvFrom(),
            offers = setOf(
              Offer(
                quantity = desc.quantityFrom(),
                totalPrice = el.priceFrom(":not(del) > .woocommerce-Price-amount"),
                sizeMl = desc.sizeMlFrom(),
                format = STANDARD_FORMAT
              )
            )

          )
        }
      }
  }

  private data class VariableParts(
    val name: String,
    val summary: String? = null,
    val mixed: Boolean = false
  )

  private fun extractVariableParts(title: String, desc: String): VariableParts {
    val parts = desc.maybe { extract("([^/]*?) / ([^/]*?) /") }
    return if (parts != null) {
      VariableParts(
        name = parts[1],
        summary = parts[2]
      )
    } else {
      VariableParts(
        name = title.cleanse(".*:", "\\d+\\s*x\\s*\\d+\\s*ml"),
        mixed = true
      )
    }
  }

  companion object {
    private val ROOT = root("https://www.howlinghops.co.uk/shop")

    private val STANDARD_FORMAT = Format.CAN
  }
}
