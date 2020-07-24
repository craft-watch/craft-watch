package watch.craft.scrapers

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.module.kotlin.convertValue
import watch.craft.Offer
import watch.craft.Scraper
import watch.craft.Scraper.ScrapedItem
import watch.craft.dsl.*
import watch.craft.utils.mapper
import java.net.URI

class AffinityScraper : Scraper {
  private val mapper = mapper()

  override val jobs = forJsonRoots(JSON_ROOT) { content: Any ->
    val idx = mapper.convertValue<Index>(content)

    idx.products
      .map { p ->
        leafJson(p.name, (JSON_ROOT.url.toString() + "/" + p.url).toUri()) { leaf ->
          val product = mapper.convertValue<Product>(leaf)

          ScrapedItem(
            name = product.name,
            desc = product.description,
            abv = product.description.abvFrom(),
            available = true,
            offers = product.variants.map { v ->
              Offer(
                quantity = v.optionsValues.quantity?.extract("\\d+")?.intFrom(0) ?: 1,
                totalPrice = v.price,
                sizeMl = product.name.sizeMlFrom(),
                format = product.name.maybe { formatFrom(fullProse = false) }
              )
            }.toSet(),
            thumbnailUrl = product.images.first().url
          )
        }
      }
  }

  private data class Index(
    val products: List<Product>
  ) {
    data class Product(
      val name: String,
      val url: String
    )
  }

  private data class Product(
    val name: String,
    val description: String,
    val images: List<Image>,
    val variants: List<Variant>
  ) {
    data class Image(
      val url: URI
    )

    data class Variant(
      val optionsValues: OptionsValues,
      val price: Double
    )

    data class OptionsValues(
      @JsonProperty("Quantity")
      val quantity: String?
    )
  }

  companion object {
    private val JSON_ROOT = root("https://affinitybrewco.com/____webshop/v1/affinitybrewco.com/products")
  }
}
