package watch.craft.scrapers

import watch.craft.Offer
import watch.craft.Scraper
import watch.craft.Scraper.Job.Leaf
import watch.craft.Scraper.ScrapedItem
import watch.craft.utils.*
import java.net.URI
import kotlin.math.max

class CloudwaterScraper : Scraper {
  override val jobs = forRootUrls(ROOT_URL) { root ->
    root
      .selectMultipleFrom(".product-collection .grid-item")
      .map { el ->
        val a = el.selectFrom("a.product-title")
        val nameLines = a.formattedTextFrom().split("\n")

        Leaf(nameLines[0], a.urlFrom()) { doc ->
          val desc = doc.formattedTextFrom(".short-description")
          val descLines = desc.split("\n")

          val allNumItems = descLines
            .mapNotNull { it.maybe { quantityFrom() } }
          val mixed = allNumItems.size > 1

          ScrapedItem(
            name = nameLines[0],
            summary = if (nameLines.size > 1) nameLines[1] else null,
            desc = desc,
            mixed = mixed,
            abv = if (mixed) null else descLines.mapNotNull { it.maybe { abvFrom() } }
              .min(), // Workaround for prose saying "100%"
            available = true,
            offers = setOf(
              Offer(
                quantity = nameLines[0].maybe { quantityFrom() } ?: max(1, allNumItems.sum()),
                totalPrice = el.priceFrom(".price-regular"),
                sizeMl = if (mixed) null else desc.sizeMlFrom()
              )
            ),
            thumbnailUrl = el.urlFrom(".product-grid-image img")
          )
        }
      }
  }

  companion object {
    private val ROOT_URL = URI("https://shop.cloudwaterbrew.co/collections/cloudwater-beer")
  }
}
