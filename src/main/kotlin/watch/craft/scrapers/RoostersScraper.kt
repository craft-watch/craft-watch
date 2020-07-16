package watch.craft.scrapers

import com.fasterxml.jackson.annotation.JsonProperty
import org.jsoup.nodes.Document
import watch.craft.Format.CAN
import watch.craft.Offer
import watch.craft.Scraper
import watch.craft.Scraper.Job.Leaf
import watch.craft.Scraper.ScrapedItem
import watch.craft.SkipItemException
import watch.craft.utils.*
import java.net.URI

class RoostersScraper : Scraper {
  override val jobs = forRootUrls(ROOT_URL) { root ->
    root
      .selectMultipleFrom(".Main--products-list .ProductList-item")
      .map { el ->
        val title = el.textFrom(".ProductList-title")

        Leaf(title, el.urlFrom(".ProductList-item-link")) { doc ->
          if (title.containsWord("bag")) {
            throw SkipItemException("Bag-in-box options are too much hassle")
          }

          val desc = doc.formattedTextFrom(".ProductItem-details-excerpt")
          val mixed = title.contains("mix", ignoreCase = true)

          ScrapedItem(
            name = title,
            summary = if (mixed) null else {
              desc.split("\n")[0].split(" - ")[0].trim()
            }, // This approach doesn't give good results for the mixed cases
            desc = desc,
            mixed = mixed,
            abv = if (mixed) null else desc.abvFrom(),
            available = true,
            offers = doc.extractOffers(desc).toSet(),
            thumbnailUrl = el.urlFrom("img.ProductList-image")
          )
        }
      }
  }

  private fun Document.extractOffers(desc: String): List<Offer> {
    val sizeMl = desc.sizeMlFrom()
    return maybe { attrFrom(".product-variants", "data-variants") }
      ?.jsonFrom<List<Variant>>()
      ?.map {
        Offer(
          totalPrice = it.salePrice.toDouble() / 100,
          quantity = it.attributes.size,
          sizeMl = sizeMl,
          format = CAN
        )
      }
      ?: listOf(
        Offer(
          totalPrice = priceFrom(".product-price"),
          quantity = desc.extract("(\\d+)\\s*x").intFrom(1),
          sizeMl = sizeMl,
          format = CAN
        ) // Fallback to assuming a single price
      )
  }

  private data class Variant(
    val attributes: Attributes,
    val price: Int,
    val salePrice: Int
  ) {
    data class Attributes(
      @JsonProperty("Size")
      val size: Int
    )
  }


  companion object {
    private val ROOT_URL = URI("https://www.roosters.co.uk/shop/beer")
  }
}
