package watch.craft.scrapers

import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import watch.craft.*
import watch.craft.Scraper.IndexEntry
import watch.craft.Scraper.ScrapedItem
import java.net.URI

class WiperAndTrueScraper : Scraper {
  override val name = "Wiper and True"
  override val rootUrls = listOf(URI("https://wiperandtrue.com/order-beer-online"))

  override fun scrapeIndex(root: Document) = root
    .selectMultipleFrom("#productList a.product")
    .map { el ->
      val rawName = el.textFrom(".product-title")

      IndexEntry(rawName, el.hrefFrom()) { doc ->
        val desc = doc.selectFrom(".product-excerpt")
        val parts = extractVariableParts(rawName, desc)

        ScrapedItem(
          name = rawName.extract("(.*?) Case")[1],
          summary = parts.summary,
          desc = desc.normaliseParagraphsFrom(),
          mixed = parts.mixed,
          sizeMl = parts.sizeMl,
          abv = parts.abv,
          available = el.maybeSelectFrom(".sold-out") == null,
          numItems = parts.numItems,
          price = el.priceFrom(".product-price"),
          thumbnailUrl = el.dataSrcFrom(".product-image img")
        )
      }
    }

  private fun extractVariableParts(rawName: String, desc: Element) =
    if (rawName.contains("mixed", ignoreCase = true)) {
      VariableParts(
        mixed = true,
        numItems = 12    // TODO - hardcoded
      )
    } else {
      val parts = desc.maybeExtractFrom("p", "(\\d+)x\\s+(\\d+)ml.*?(\\d(\\.\\d+)?)%\\s+(.*)\\.")
        ?: throw SkipItemException("Can't find details, so assuming it's not a beer")
      VariableParts(
        mixed = false,
        summary = parts[5],
        abv = parts[3].toDouble(),
        sizeMl = parts[2].toInt(),
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
}
