package choliver.neapi.scrapers

import choliver.neapi.*
import choliver.neapi.Scraper.IndexEntry
import choliver.neapi.Scraper.Result.Item
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
        val parts = rawSummary.maybeExtract("Sold as: ((\\d+) x )?(\\d+)ml")
        val numCans = parts?.get(2)?.toIntOrNull() ?: 1

        Item(
          thumbnailUrl = a.srcFrom(".attachment-woocommerce_thumbnail"),
          name = name,
          summary = rawSummary.maybeExtract("Style: (.*) ABV")?.get(1),
          available = true, // TODO
          abv = rawSummary.maybeExtract("ABV: (\\d+(\\.\\d+)?)%")?.get(1)?.toDouble(),
          sizeMl = parts?.get(3)?.toInt(),
          perItemPrice = el.ownTextFrom(".woocommerce-Price-amount").toDouble().divideAsPrice(numCans)
        )
      }
    }
}
