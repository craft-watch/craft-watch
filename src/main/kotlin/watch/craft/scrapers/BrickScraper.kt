package watch.craft.scrapers

import org.jsoup.nodes.Element
import watch.craft.Offer
import watch.craft.Scraper
import watch.craft.Scraper.Job.Leaf
import watch.craft.Scraper.ScrapedItem
import watch.craft.SkipItemException
import watch.craft.shopify.shopifyItems
import watch.craft.utils.*
import java.net.URI
import kotlin.text.RegexOption.IGNORE_CASE

class BrickScraper : Scraper {
  override val jobs = forPaginatedRootUrl(ROOT_URL) { root ->
    root
      .shopifyItems()
      .map { details ->
        Leaf(details.title, details.url) { doc ->
          val desc = doc.selectFrom(".product-single__description")
          val mixed = details.title.contains("mixed", ignoreCase = true)
          val abv = desc.maybe { abvFrom() }
          if (!mixed && abv == null) {
            throw SkipItemException("Can't find ABV, so assuming not a beer")
          }

          val attributes = desc.extractAttributes()

          ScrapedItem(
            name = details.title
              .replace("^\\d+\\s*x".toRegex(IGNORE_CASE), "")
              .replace("case", "", ignoreCase = true)
              .replace("[(].*[)]".toRegex(), "")
              .trim(),
            summary = attributes["beer style"],
            desc = desc.formattedTextFrom(),
            mixed = mixed,
            abv = if (mixed) null else abv,
            available = details.available,
            offers = setOf(
              Offer(
                quantity = details.title.maybe { extract("(\\d+)\\s*x").intFrom(1) } ?: 1,
                totalPrice = details.price,
                sizeMl = desc.maybe { sizeMlFrom() },
                format = desc.formatFrom()
              )
            ),
            thumbnailUrl = details.thumbnailUrl
          )
        }
      }
  }

  private fun Element.extractAttributes() = selectMultipleFrom("tr")
    .associate { it.textFrom("td:first-child").toLowerCase() to it.textFrom("td:last-child") }

  companion object {
    val ROOT_URL = URI("https://shop.brickbrewery.co.uk/collections/frontpage")
  }
}
