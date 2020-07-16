package watch.craft.scrapers

import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import watch.craft.Format.KEG
import watch.craft.Offer
import watch.craft.Scraper
import watch.craft.Scraper.Job.Leaf
import watch.craft.Scraper.ScrapedItem
import watch.craft.utils.*
import java.net.URI
import kotlin.text.RegexOption.IGNORE_CASE

class PurityScraper : Scraper {
  override val jobs = forRootUrls(ROOT_URL) { root ->
    root
      .extractNonMixedProducts()
      .map { el ->
        val title = el.textFrom(".woocommerce-loop-product__title")

        Leaf(title, el.urlFrom(".woocommerce-LoopProduct-link")) { doc ->
          val desc = doc.formattedTextFrom(".elementor-widget-woocommerce-product-content")

          ScrapedItem(
            name = title
              .replace(CASK_OR_POLYPIN_REGEX, "")
              .replace("^purity".toRegex(IGNORE_CASE), "")
              .trim(),
            summary = null,
            desc = desc,
            abv = desc.extractAbv(),
            available = true,
            offers = doc.extractOffers(title, desc).toSet(),
            thumbnailUrl = el.attrFrom(".attachment-woocommerce_thumbnail", "data-lazy-src").toUri()
          )
        }
      }
  }

  // Mixed packs live in "pub experience" section, and they're too messy to extract structured info from
  private fun Document.extractNonMixedProducts() = this
    .selectMultipleFrom("section")
    .filterNot { it.maybe { textFrom("h1").containsWord("experience") } ?: true }
    .flatMap { it.selectMultipleFrom(".product") }

  private fun String.extractAbv() = this
    .split("\n")
    .mapNotNull { it.maybe { abvFrom() } }
    .min()

  private fun Document.extractOffers(title: String, desc: String): List<Offer> {
    val rows = maybe { selectMultipleFrom(".woocommerce-grouped-product-list-item") }
    return if (rows != null) {
      extractOffersFromTable(rows)
    } else {
      extractOfferFromDesc(title, desc)
    }
  }

  private fun extractOffersFromTable(rows: Elements): List<Offer> {
    val labelledPrices = rows.associate { row ->
      val label = row.textFrom(".woocommerce-grouped-product-list-item__label")
      val price = row.priceFrom(":not(del) > .woocommerce-Price-amount")  // Ensure we get the sale price
      label to price
    }

    val format = labelledPrices.entries
      .map { it.key.maybe { formatFrom(fullProse = false) } }
      .firstOrNull { it != null }

    val sizeMl = labelledPrices.entries
      .map { it.key.maybe { sizeMlFrom() } }
      .firstOrNull { it != null }

    return labelledPrices.map { (label, price) ->
      val allNumbers = "\\d+".toRegex().findAll(label).map { it.value.toInt() }.toList()

      Offer(
        quantity = allNumbers.firstOrNull { it < 100 } ?: 1,
        totalPrice = price,
        sizeMl = sizeMl,
        format = format
      )
    }
  }

  private fun Document.extractOfferFromDesc(title: String, desc: String) = listOf(
    Offer(
      quantity = desc.maybe { extract("(\\d+)\\s*x").intFrom(1) } ?: 1,
      totalPrice = priceFrom(".price"),
      // Hardcoded for keggy things because the size isn't always in the desc
      sizeMl = when {
        title.contains(CASK_REGEX) -> 5_000
        title.contains(POLYPIN_REGEX) -> 20_000
        else -> desc.maybe { sizeMlFrom() }
      },
      format = when {
        title.contains(CASK_OR_POLYPIN_REGEX) -> KEG
        else -> desc.formatFrom()
      }
    )
  )

  companion object {
    private val ROOT_URL = URI("https://puritybrewing.com/product-category/purity/")

    private val CASK_OR_POLYPIN_REGEX = "(mini cask|polypin)$".toRegex(IGNORE_CASE)
    private val POLYPIN_REGEX = "polypin".toRegex(IGNORE_CASE)
    private val CASK_REGEX = "mini cask".toRegex(IGNORE_CASE)
  }
}
