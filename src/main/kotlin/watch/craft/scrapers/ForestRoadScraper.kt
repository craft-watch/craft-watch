package watch.craft.scrapers

import watch.craft.Format.*
import watch.craft.Offer
import watch.craft.Scraper
import watch.craft.Scraper.Job.Leaf
import watch.craft.Scraper.ScrapedItem
import watch.craft.SkipItemException
import watch.craft.utils.*
import java.net.URI
import kotlin.math.max
import kotlin.text.RegexOption.IGNORE_CASE

class ForestRoadScraper : Scraper {
  override val jobs = forRootUrls(*ROOT_URLS) { root ->
    root
      .selectMultipleFrom(".Main--products-list .ProductList-item")
      .map { el ->
        val title = el.textFrom(".ProductList-title")

        Leaf(title, el.hrefFrom("a.ProductList-item-link")) { doc ->
          if (title.contains("subscription", ignoreCase = true)) {
            throw SkipItemException("Subscriptions aren't something we can model")
          }

          val desc = doc.formattedTextFrom(".ProductItem-details-excerpt").toTitleCase()
          val descLines = desc.split("\n")
          val mixed = title.contains("mixed", ignoreCase = true)

          ScrapedItem(
            name = title
              .replace("[(].*[)]".toRegex(), "")
              .replace("cans".toRegex(IGNORE_CASE), "")
              .trim()
              .toTitleCase(),
            summary = if (descLines[0].contains("@")) null else descLines[0], // Filter out nonsense
            desc = desc,
            mixed = mixed,
            abv = if (mixed) null else desc.orSkip("No ABV, so assume not a beer") { abvFrom() },
            available = true,
            offers = setOf(
              Offer(
                quantity = title.maybe { extractQuantity() }
                  ?: max(1, descLines.mapNotNull { it.maybe { extractQuantity() } }.sum()),
                totalPrice = el.priceFrom(".product-price"),
                sizeMl = title.maybe { sizeMlFrom() } ?: desc.maybe { sizeMlFrom() },
                format = when {
                  title.contains("keg", ignoreCase = true) -> KEG
                  title.contains("cans", ignoreCase = true) -> CAN
                  else -> BOTTLE
                }
              )
            ),
            thumbnailUrl = el.dataSrcFrom("img.ProductList-image")
          )
        }
      }
  }

  private fun String.extractQuantity() = extract("(\\d+)\\s*x").intFrom(1)

  companion object {
    val ROOT_URLS = arrayOf(
      URI("https://www.forestroad.co.uk/shop?category=BEER"),
      URI("https://www.forestroad.co.uk/shop?category=SPECIAL")
    )
  }
}
