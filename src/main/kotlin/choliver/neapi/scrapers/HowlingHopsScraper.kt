package choliver.neapi.scrapers

import choliver.neapi.ParsedItem
import choliver.neapi.Scraper
import choliver.neapi.Scraper.Context
import java.net.URI

class HowlingHopsScraper : Scraper {
  override val name = "Howling Hops"

  override fun Context.scrape() = request(ROOT_URL)
    .selectFirst(".wc-block-handpicked-products") // Avoid apparel
    .select(".wc-block-grid__product")
    .map { el ->
      val a = el.selectFirst(".wc-block-grid__product-link")
      val url = a.hrefFrom()
      val thumbnailUrl = a.srcFrom(".attachment-woocommerce_thumbnail")
      val price = el.select(".woocommerce-Price-amount")
        .filterNot { it.parent().tagName() == "del" } // Avoid non-sale price
        .first()
        .ownText()
        .toDouble()

      val itemDoc = request(url)
      val available = itemDoc.textFrom(".stock") == "In stock"

      val shortDesc = itemDoc.textFrom(".woocommerce-product-details__short-description")
      val parts = shortDesc.extract("([^/]*?) / ([^/]*?) / (\\d+) x (\\d+)ml / (\\d+(\\.\\d+)?)% ABV")
      if (parts != null) {
        ParsedItem(
          thumbnailUrl = thumbnailUrl,
          url = url,
          name = parts[1],
          summary = parts[2],
          available = available,
          sizeMl = parts[4].toInt(),
          abv = parts[5].toDouble(),
          perItemPrice = price.divideAsPrice(parts[3].toInt())
        )
      } else {
        with(shortDesc.extract("(.*?) (\\d+) x (\\d+)ml")!!) {
          val numCans = this[2].toInt()
          ParsedItem(
            thumbnailUrl = thumbnailUrl,
            url = url,
            name = this[1],
            summary = "${numCans} cans",
            available = available,
            sizeMl = this[3].toInt(),
            abv = null,
            perItemPrice = price.divideAsPrice(numCans)
          )
        }
      }
    }
    .groupBy { it.name }
    .values
    .map { group -> group.minBy { it.perItemPrice }!! }  // Find best price for this beer

  companion object {
    private val ROOT_URL = URI("https://www.howlinghops.co.uk/shop")
  }
}
