package watch.craft.scrapers

import com.fasterxml.jackson.annotation.JsonProperty
import org.jsoup.nodes.Document
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

class BrixtonScraper : Scraper {
  override val jobs = forRootUrls(ROOT_URL) { root ->
    root
      .selectMultipleFrom(".products .product")
      .map { el ->
        val title = el.textFrom(".woocommerce-loop-product__title")

        Leaf(title, el.hrefFrom(".woocommerce-LoopProduct-link")) { doc ->
          val mixed = title.contains("mixed", ignoreCase = true)

          ScrapedItem(
            name = title.toTitleCase(),
            summary = null,
            desc = doc.formattedTextFrom(".woocommerce-product-details__short-description"),
            mixed = mixed,
            abv = if (mixed) null else doc.abvFrom(".woocommerce-product-attributes"),
            available = true,
            offers = doc.extractOffers().toSet(),
            thumbnailUrl = el.srcFrom(".attachment-woocommerce_thumbnail")
          )
        }
      }
  }

  private fun Document.extractOffers(): List<Offer> {
    return attrFrom(".variations_form", "data-product_variations")
      .jsonFrom<List<Variant>>()
      .also { it.forEach { println(it) } }
      .map {
        val parts = it.sku.extract("(\\d+)x(\\d+)")
        Offer(
          totalPrice = it.displayPrice,
          quantity = parts.intFrom(1),
          sizeMl = parts.intFrom(2),
          format = it.sku.formatFrom()
        )
      }
  }


  private data class Variant(
    @JsonProperty("display_price")
    val displayPrice: Double,
    val sku: String,
    val attributes: Attributes
  ) {
    data class Attributes(
      @JsonProperty("attribute_pack-type")
      val packType: String
    )
  }

  companion object {
    private val ROOT_URL = URI("https://www.brixtonbrewery.com/product-category/beers/")
  }
}
