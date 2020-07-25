package watch.craft.scrapers

import watch.craft.Offer
import watch.craft.Scraper

import watch.craft.Scraper.Node.ScrapedItem
import watch.craft.dsl.*

class CloudwaterScraper : Scraper {
  override val roots = fromHtmlRoots(ROOT) { root ->
    root
      .selectMultipleFrom(".product-collection .grid-item")
      .map { el ->
        val a = el.selectFrom("a.product-title")
        val nameLines = a.formattedTextFrom().split("\n")

        fromHtml(nameLines[0], a.urlFrom()) { doc ->
          val desc = doc.formattedTextFrom(".short-description")
          val allNumItems = desc.collectFromLines { quantityFrom() }
          val mixed = allNumItems.size > 1

          ScrapedItem(
            name = nameLines[0],
            summary = if (nameLines.size > 1) nameLines[1] else null,
            desc = desc,
            mixed = mixed,
            abv = if (mixed) null else desc.collectFromLines { abvFrom() }.min(), // Workaround for prose saying "100%"
            available = true,
            offers = setOf(
              Offer(
                quantity = nameLines[0].maybe { quantityFrom() } ?: 1,
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
    private val ROOT = root("https://shop.cloudwaterbrew.co/collections/cloudwater-beer")
  }
}
