package watch.craft.scrapers

import org.jsoup.nodes.Element
import watch.craft.Offer
import watch.craft.Scraper

import watch.craft.Scraper.Node.ScrapedItem
import watch.craft.dsl.*

class WiperAndTrueScraper : Scraper {
  override val roots = fromHtmlRoots(ROOT) { root ->
    root
      .selectMultipleFrom("#productList a.product")
      .map { el ->
        val rawName = el.textFrom(".product-title")

        fromHtml(rawName, el.urlFrom()) { doc ->
          val desc = doc.selectFrom(".product-excerpt")
          val parts = extractVariableParts(rawName, desc)

          ScrapedItem(
            name = rawName.extract("(.*?) Case")[1],
            summary = parts.summary,
            desc = desc.formattedTextFrom(),
            mixed = parts.mixed,
            abv = parts.abv,
            available = ".sold-out" !in el,
            offers = setOf(
              Offer(
                quantity = parts.numItems,
                totalPrice = el.priceFrom(".product-price"),
                sizeMl = parts.sizeMl
              )
            ),
            thumbnailUrl = el.urlFrom(".product-image img")
          )
        }
      }
  }

  private fun extractVariableParts(rawName: String, desc: Element) =
    if (rawName.containsMatch("mixed")) {
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
    private val ROOT = root("https://wiperandtrue.com/order-beer-online")
  }
}
