package watch.craft.scrapers

import org.jsoup.nodes.Document
import watch.craft.Offer
import watch.craft.Scraper
import watch.craft.Scraper.Node.ScrapedItem
import watch.craft.dsl.*

class AnspachAndHobdayScraper : Scraper {
  override val roots = fromHtmlRoots(*ROOTS) { root, keg ->
    root()
      .selectMultipleFrom(".product")
      .map { el ->
        val title = el.textFrom(".product-title")
        fromHtml(title, el.urlFrom()) { doc ->
          val abv = title.orSkip("No ABV, so assume not a beer") { abvFrom() }
          val parts = title.cleanse(".*:").split(" - ")
          val desc = doc().formattedTextFrom(".product-excerpt")

          ScrapedItem(
            name = if (parts.size > 2) parts[1] else parts[0],
            summary = if (parts.size > 2) parts[0] else null,
            desc = desc,
            abv = abv,
            available = ".sold-out" !in el,
            offers = doc().extractOffers(desc).toSet(),
            thumbnailUrl = el.urlFrom("img") { "$it?format=200w" }
          )
        }
      }
  }

  private fun Document.extractOffers(desc: String): List<Offer> {
    val sizeMl = desc.maybe { sizeMlFrom() }
    val format = desc.maybe { formatFrom() }

    return attrFrom("#productDetails", "data-variants").jsonFrom<List<Variant>>().map {
      Offer(
        quantity = it.sku.extract("\\d+").intFrom(0),
        totalPrice = it.price / 100.0,
        sizeMl = sizeMl,
        format = format
      )
    }
  }

  private data class Variant(
    val sku: String,
    val price: Int
  )

  companion object {
    private val ROOTS = arrayOf(
      root("https://www.anspachandhobday.com/")
    )
  }
}
