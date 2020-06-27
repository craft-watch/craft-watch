package choliver.neapi.scrapers

import choliver.neapi.*
import choliver.neapi.Scraper.IndexEntry
import choliver.neapi.Scraper.Item
import org.jsoup.nodes.Document
import java.net.URI

class GipsyHillScraper : Scraper {
  override val name = "Gipsy Hill"
  override val rootUrl = URI("https://gipsyhillbrew.com")

  override fun scrapeIndex(root: Document) = root
    .selectMultipleFrom(".product")
    .map { el ->
      val a = el.selectFrom(".woocommerce-LoopProduct-link")
      val name = a.textFrom(".woocommerce-loop-product__title")

      IndexEntry(name, a.hrefFrom()) { doc ->
        val rawSummary = doc.textFrom(".summary")
        val numCans = with(doc.maybeSelectMultipleFrom(".woosb-title-inner")) {
          if (isEmpty()) {
            1
          } else {
            map { it.extractFrom(regex = "(\\d+) ×")[1].toInt() }.sum()
          }
        }
        val style = rawSummary.maybeExtract("Style: (.*) ABV")?.get(1)

        Item(
          thumbnailUrl = a.srcFrom(".attachment-woocommerce_thumbnail"),
          name = name,
          summary = style,
          available = true, // TODO
          abv = if (style in listOf("Various", "Mixed")) {
            null
          } else {
            rawSummary.maybeExtract("ABV: (\\d+(\\.\\d+)?)%")?.get(1)?.toDouble()
          },
          sizeMl = rawSummary.maybeExtract("Sold as: .*?(\\d+)ml")?.get(1)?.toInt(),
          perItemPrice = el.ownTextFrom(".woocommerce-Price-amount").toDouble().divideAsPrice(numCans)
        )
      }
    }
}
