package watch.craft.scrapers

import watch.craft.*
import watch.craft.Scraper.Job.Leaf
import watch.craft.Scraper.ScrapedItem
import java.net.URI

class HowlingHopsScraper : Scraper {
  override val brewery = Brewery(
    shortName = "Howling Hops",
    name = "Howling Hops",
    location = "Hackney Wick, London",
    websiteUrl = URI("https://www.howlinghops.co.uk/")
  )

  override val jobs = forRootUrls(ROOT_URL) { root ->
    root
      .selectFrom(".wc-block-handpicked-products") // Avoid apparel
      .selectMultipleFrom(".wc-block-grid__product")
      .map { el ->
        val a = el.selectFrom(".wc-block-grid__product-link")
        val rawName = el.textFrom(".wc-block-grid__product-title")

        Leaf(rawName, a.hrefFrom()) { doc ->
          val parts = extractVariableParts(doc.textFrom(".woocommerce-product-details__short-description"))

          ScrapedItem(
            thumbnailUrl = a.srcFrom(".attachment-woocommerce_thumbnail"),
            name = parts.name,
            summary = parts.summary,
            desc = doc.maybeWholeTextFrom(".woocommerce-product-details__short-description"),
            mixed = parts.mixed,
            available = doc.textFrom(".stock") == "In stock",
            sizeMl = parts.sizeMl,
            abv = parts.abv,
            numItems = parts.numCans,
            price = el.selectMultipleFrom(".woocommerce-Price-amount")
              .filterNot { it.parent().tagName() == "del" } // Avoid non-sale price
              .first()
              .ownText()
              .toDouble()
          )
        }
      }
  }

  private data class VariableParts(
    val name: String,
    val summary: String? = null,
    val abv: Double? = null,
    val sizeMl: Int,
    val numCans: Int,
    val mixed: Boolean = false
  )

  private fun extractVariableParts(desc: String): VariableParts {
    val parts = desc.maybeExtract("([^/]*?) / ([^/]*?) / (\\d+) x (\\d+)ml / (\\d+(\\.\\d+)?)% ABV")
    return if (parts != null) {
      VariableParts(
        name = parts[1],
        summary = parts[2],
        sizeMl = parts[4].toInt(),
        abv = parts[5].toDouble(),
        numCans = parts[3].toInt()
      )
    } else {
      val betterParts = desc.extract("(.*?) (\\d+) x (\\d+)ml")
      val numCans = betterParts[2].toInt()
      VariableParts(
        name = betterParts[1],
        sizeMl = betterParts[3].toInt(),
        numCans = numCans,
        mixed = true
      )
    }
  }

  companion object {
    private val ROOT_URL = URI("https://www.howlinghops.co.uk/shop")
  }
}
