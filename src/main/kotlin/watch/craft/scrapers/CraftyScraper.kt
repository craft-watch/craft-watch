package watch.craft.scrapers

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonProperty
import org.jsoup.nodes.Document
import watch.craft.Offer
import watch.craft.Scraper
import watch.craft.Scraper.Job.Leaf
import watch.craft.Scraper.ScrapedItem
import watch.craft.dsl.*
import java.net.URI

class CraftyScraper : Scraper {
  override val jobs = forPaginatedRootUrl(ROOT_URL) { root ->
    root
      .selectFrom("ul.products")  // Later sections are mostly overlapping
      .selectMultipleFrom(".product")
      .map { el ->
        val title = el.textFrom(".woocommerce-loop-product__title")

        Leaf(title, el.urlFrom(".woocommerce-LoopProduct-link")) { doc ->
          val mixed = doc.containsMatchFrom(".product_meta", "mixed")
          val desc = doc.formattedTextFrom(".et_pb_wc_description")

          ScrapedItem(
            name = title.cleanse("[(].*[)]", "\\s–\\s.*"),
            summary = null, // No obviously-useful info to extract
            desc = desc,
            mixed = mixed,
            abv = if (mixed) null else desc.abvFrom(),
            available = ".woosticker_sold" !in el,
            offers = doc.extractOffers(desc, mixed).toSet(),
            thumbnailUrl = el.urlFrom(".attachment-woocommerce_thumbnail")
          )
        }
      }
  }

  private fun Document.extractOffers(desc: String, mixed: Boolean): List<Offer> {
    val sizeMl = desc.sizeMlFrom()
    val format = desc.formatFrom()
    return maybe { attrFrom(".variations_form", "data-product_variations") }
      ?.jsonFrom<List<Variant>>()
      ?.map {
        Offer(
          totalPrice = it.displayPrice,
          quantity = it.attributes.packSize.extract("\\d+").intFrom(0),
          sizeMl = sizeMl,
          format = format
        )
      }
      ?: listOf(
        Offer(
          totalPrice = priceFrom(".price"),
          quantity = if (mixed) desc.quantityFrom() else 1,
          sizeMl = sizeMl,
          format = format
        ) // Fallback to assuming a single price
      )
  }

  private fun String.quantityFrom() = HARDCODED_QUANTITIES.entries.first { containsWord(it.key) }.value

  private data class Variant(
    @JsonProperty("display_price")
    val displayPrice: Double,
    val attributes: Attributes
  ) {
    data class Attributes(
      @JsonProperty("attribute_pa_pack-size")
      @JsonAlias("attribute_pa_small-pack-size")
      val packSize: String
    )
  }

  companion object {
    val ROOT_URL = URI("https://www.craftybrewing.co.uk/crafty-core-beers/")

    private val HARDCODED_QUANTITIES = mapOf(
      "six" to 6,
      "dozen" to 12
    )
  }
}
