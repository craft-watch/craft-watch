package choliver.neapi.scrapers

import choliver.neapi.*
import choliver.neapi.Scraper.Context
import java.net.URI

class HowlingHopsScraper : Scraper {
  override val name = "Howling Hops"

  override fun Context.scrape() = request(ROOT_URL)
    .selectFrom(".wc-block-handpicked-products") // Avoid apparel
    .selectMultipleFrom(".wc-block-grid__product")
    .map { el ->
      val a = el.selectFrom(".wc-block-grid__product-link")
      val url = a.hrefFrom()
      val thumbnailUrl = a.srcFrom(".attachment-woocommerce_thumbnail")
      val price = el.selectMultipleFrom(".woocommerce-Price-amount")
        .filterNot { it.parent().tagName() == "del" } // Avoid non-sale price
        .first()
        .ownText()
        .toDouble()

      val itemDoc = request(url)
      val parts = extractVariableParts(itemDoc.textFrom(".woocommerce-product-details__short-description"))
      ScrapedItem(
        thumbnailUrl = thumbnailUrl,
        url = url,
        name = parts.name,
        summary = parts.summary,
        available = itemDoc.textFrom(".stock") == "In stock",
        sizeMl = parts.sizeMl,
        abv = parts.abv,
        perItemPrice = price.divideAsPrice(parts.numCans)
      )
    }
    .bestPricedItems()

  private data class VariableParts(
    val name: String,
    val summary: String,
    val abv: Double? = null,
    val sizeMl: Int,
    val numCans: Int
  )

  private fun extractVariableParts(desc: String): VariableParts {
    val parts = desc.extract("([^/]*?) / ([^/]*?) / (\\d+) x (\\d+)ml / (\\d+(\\.\\d+)?)% ABV")
    return if (parts != null) {
      VariableParts(
        name = parts[1],
        summary = parts[2],
        sizeMl = parts[4].toInt(),
        abv = parts[5].toDouble(),
        numCans = parts[3].toInt()
      )
    } else {
      val betterParts = desc.extract("(.*?) (\\d+) x (\\d+)ml")!!
      val numCans = betterParts[2].toInt()
      VariableParts(
        name = betterParts[1],
        summary = "${numCans} cans",
        sizeMl = betterParts[3].toInt(),
        numCans = numCans
      )
    }
  }

  companion object {
    private val ROOT_URL = URI("https://www.howlinghops.co.uk/shop")
  }
}
