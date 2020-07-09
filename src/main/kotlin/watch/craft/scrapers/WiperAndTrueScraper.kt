package watch.craft.scrapers

import org.jsoup.nodes.Element
import watch.craft.Brewery
import watch.craft.Scraper
import watch.craft.Scraper.Job.Leaf
import watch.craft.Scraper.ScrapedItem
import watch.craft.utils.*
import java.net.URI

class WiperAndTrueScraper : Scraper {
  override val brewery = Brewery(
    shortName = "Wiper and True",
    name = "Wiper and True",
    location = "Bristol",
    websiteUrl = URI("https://wiperandtrue.com/")
  )

  override val rateLimitPeriodMillis = 2000

  override val jobs = forRootUrls(ROOT_URL) { root ->
    root
      .selectMultipleFrom("#productList a.product")
      .map { el ->
        val rawName = el.textFrom(".product-title")

        Leaf(rawName, el.hrefFrom()) { doc ->
          val desc = doc.selectFrom(".product-excerpt")
          val parts = extractVariableParts(rawName, desc)

          ScrapedItem(
            name = rawName.extract("(.*?) Case")[1],
            summary = parts.summary,
            desc = desc.formattedTextFrom(),
            mixed = parts.mixed,
            sizeMl = parts.sizeMl,
            abv = parts.abv,
            available = ".sold-out" !in el,
            quantity = parts.numItems,
            totalPrice = el.priceFrom(".product-price"),
            thumbnailUrl = el.dataSrcFrom(".product-image img")
          )
        }
      }
  }

  private fun extractVariableParts(rawName: String, desc: Element) =
    if (rawName.contains("mixed", ignoreCase = true)) {
      VariableParts(
        mixed = true,
        numItems = 12    // TODO - hardcoded
      )
    } else {
      val parts = desc.orSkip("Can't find details, so assuming it's not a beer") {
        extractFrom("p", "(\\d+).*?%\\s+(.*)\\.")
      }
      VariableParts(
        mixed = false,
        summary = parts[2],
        abv = desc.abvFrom(),
        sizeMl = desc.sizeMlFrom(),
        numItems = parts[1].toInt()
      )
    }

  private data class VariableParts(
    val mixed: Boolean,
    val summary: String? = null,
    val abv: Double? = null,
    val sizeMl: Int? = null,
    val numItems: Int
  )

  companion object {
    private val ROOT_URL = URI("https://wiperandtrue.com/order-beer-online")
  }
}
