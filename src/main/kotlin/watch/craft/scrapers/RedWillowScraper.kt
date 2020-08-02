package watch.craft.scrapers

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonProperty
import org.jsoup.nodes.Document
import watch.craft.Offer
import watch.craft.Scraper

import watch.craft.Scraper.Node.ScrapedItem
import watch.craft.SkipItemException
import watch.craft.dsl.*

class RedWillowScraper : Scraper {
  override val roots = fromHtmlRoots(ROOT) { root ->
    root()
      .selectMultipleFrom(".ProductList-grid .ProductList-item")
      .map { el ->
        val rawName = el.textFrom(".ProductList-title")

        fromHtml(rawName, el.urlFrom("a.ProductList-item-link")) { doc ->
          rawName.skipIfOnBlacklist(*BLACKLIST)

          val desc = doc().formattedTextFrom(".ProductItem-details-excerpt")
          val sizeMl = (rawName + "\n" + desc).maybe { sizeMlFrom() }

          ScrapedItem(
            name = rawName.extract("^[A-Za-z-'\\s]+")[0].trim().toTitleCase(),
            summary = rawName.maybe { extract("%\\s+([^\\d]+)$")[1] },  // In some cases, beer type is after the ABV
            desc = desc.ifBlank { null },
            mixed = rawName.containsMatch("mixed"),
            abv = rawName.maybe { abvFrom() },
            available = true,
            offers = doc().extractOffers().map { it.copy(sizeMl = sizeMl) }.toSet(),
            thumbnailUrl = doc().extractSmallThumbnail()
          )
        }
      }
  }

  // By default, the images are huge!
  private fun Document.extractSmallThumbnail() =
    urlFrom("img.ProductItem-gallery-slides-item-image") { "$it?format=200w" }

  private fun Document.extractOffers() =
    maybe { attrFrom(".product-variants", "data-variants") }
      ?.jsonFrom<List<Variant>>()
      ?.map { Offer(totalPrice = it.price.toDouble() / 100, quantity = it.attributes.quantity) }
      ?: listOf(
        Offer(
          totalPrice = priceFrom(".product-price"),
          quantity = quantityFrom(".ProductItem-details-title")
        ) // Fallback to assuming a single price
      )

  private data class Variant(
    val attributes: Attributes,
    val price: Int
  ) {
    data class Attributes(
      @JsonProperty("Quantity")
      @JsonAlias("Quanity")   // Sigh
      val quantity: Int
    )
  }

  companion object {
    private val ROOT = root("https://www.redwillowbrewery.com/shop")

    private val BLACKLIST = arrayOf("glassware")
  }
}
