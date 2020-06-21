package choliver.neapi.scrapers

import choliver.neapi.ParsedItem
import choliver.neapi.Scraper
import choliver.neapi.Scraper.Context
import java.math.BigDecimal
import java.net.URI

class HowlingHopsScraper : Scraper {
  override val name = "Howling Hops"

  override fun Context.scrape() = request(ROOT_URL)
    .selectFirst(".wc-block-handpicked-products") // Avoid apparel
    .select(".wc-block-grid__product")
    .map { el ->
      val a = el.selectFirst(".wc-block-grid__product-link")
      val url = URI(a.attr("href").trim())

      val shortDesc = request(url)
        .selectFirst(".woocommerce-product-details__short-description")
        .text()

      val parts = shortDesc.extract("([^/]*?) / ([^/]*?) / (\\d+) x (\\d+)ml / (\\d+(\\.\\d+)?)% ABV")
      val name: String
      val summary: String?
      val abv: BigDecimal?
      val sizeMl: Int?
      val numCans: Int
      if (parts != null) {
        name = parts[1].trim()
        summary = parts[2].trim()
        numCans = parts[3].toInt()
        sizeMl = parts[4].toInt()
        abv = parts[5].toBigDecimal()
      } else {
        with(shortDesc.extract("(.*?) (\\d+) x (\\d+)ml")!!) {
          name = this[1].trim()
          numCans = this[2].toInt()
          sizeMl = this[3].toInt()
          summary = "${numCans} cans"
          abv = null
        }
      }

      ParsedItem(
        thumbnailUrl = URI(a.selectFirst(".attachment-woocommerce_thumbnail").attr("src").trim()),
        url = url,
        name = name,
        summary = summary,
        available = true, // TODO
        abv = abv,
        sizeMl = sizeMl,
        pricePerCan = el.select(".woocommerce-Price-amount")
          .filterNot { it.parent().tagName() == "del" } // Avoid non-sale price
          .first()
          .ownText()
          .toBigDecimal() / numCans.toBigDecimal()
      )
    }
    .groupBy { it.name }
    .values
    .map { group -> group.minBy { it.pricePerCan }!! }  // Find best price for this beer

  companion object {
    private val ROOT_URL = URI("https://www.howlinghops.co.uk/shop")
  }
}
