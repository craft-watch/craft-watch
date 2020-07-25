package watch.craft.scrapers

import org.jsoup.nodes.Document
import watch.craft.Offer
import watch.craft.Scraper
import watch.craft.Scraper.Node.ScrapedItem
import watch.craft.SkipItemException
import watch.craft.dsl.*

class BigDropScraper : Scraper {
  override val root = fromHtmlRoots(ROOT) { root: Document ->
    root
      .selectMultipleFrom(".products")
      .dropLast(1)  // Avoid merchandise
      .flatMap { it.selectMultipleFrom(".thumbnail") }
      .map { el ->
        val title = el.textFrom(".title")

        fromHtml(title, el.urlFrom("a")) { doc ->
          if (title.containsWord(*BLACKLIST.toTypedArray())) {
            throw SkipItemException("Not a beer")
          }

          val parts = title.split(" - ")
          val desc = doc.formattedTextFrom(".description")
          val basicPrice = el.attrFrom("meta[itemprop=price]", "content").priceFrom()

          ScrapedItem(
            name = if (parts.size > 1) parts[0] else title,
            summary = if (parts.size > 1) parts[1] else null,
            desc = desc,
            mixed = title.containsWord("sampler"),
            abv = BIG_DROP_ABV,
            available = ".sold_out" !in el,
            offers = doc.extractOffers(desc, basicPrice).toSet(),
            thumbnailUrl = el.urlFrom("img", preference = "src")
          )
        }
      }
  }

  private fun Document.extractOffers(desc: String, basicPrice: Double): List<Offer> {
    val variants = maybe {
      attrFrom(".product_form", "data-product").jsonFrom<Data>().variants
    }

    return if (variants == null || variants.first().title.containsWord("default")) {
      listOf(
        Offer(
          quantity = desc.quantityFrom(),
          totalPrice = basicPrice,
          sizeMl = desc.sizeMlFrom(),
          format = desc.formatFrom()
        )
      )
    } else {
      variants
        .map { variant ->
          Offer(
            quantity = variant.title.quantityFrom(),
            totalPrice = variant.price / 100.0,
            sizeMl = variant.sku.extract("-(\\d+)(?:-|$)").intFrom(1),
            format = variant.title.formatFrom(fullProse = false)
          )
        }
    }
  }

  private data class Data(
    val variants: List<Variant>
  ) {
    data class Variant(
      val title: String,
      val sku: String,
      val price: Int
    )
  }

  companion object {
    private val ROOT = root("https://shop.bigdropbrew.com/")

    private const val BIG_DROP_ABV = 0.5

    private val BLACKLIST = listOf("towel")   // Really wtf
  }
}
