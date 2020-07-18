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

class BrickScraper : Scraper {
  override val jobs = forPaginatedRootUrl(ROOT_URL) { root ->
    root
      .shopifyItems()
      .map { details ->
        Leaf(details.title, details.url) { doc ->
          val desc = doc.selectFrom(".product-single__description")
          val mixed = details.title.containsMatch("mixed")
          val abv = desc.maybe { abvFrom() }
          if (!mixed && abv == null) {
            throw SkipItemException("Can't find ABV, so assuming not a beer")
          }

          val attributes = desc.extractAttributes()

          ScrapedItem(
            name = details.title.cleanse("^\\d+\\s*x", "case", "[(].*[)]"),
            summary = attributes["beer style"],
            desc = desc.formattedTextFrom(),
            mixed = mixed,
            abv = if (mixed) null else abv,
            available = details.available,
            offers = setOf(
              Offer(
                quantity = details.title.maybe { quantityFrom() } ?: 1,
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
    private val ROOT_URL = URI("https://shop.brickbrewery.co.uk/collections/frontpage")
  }
}
