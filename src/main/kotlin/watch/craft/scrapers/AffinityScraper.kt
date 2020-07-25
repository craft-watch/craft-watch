package watch.craft.scrapers

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.module.kotlin.convertValue
import org.jsoup.Jsoup
import watch.craft.Format
import watch.craft.Format.*
import watch.craft.Offer
import watch.craft.Scraper
import watch.craft.Scraper.Node.ScrapedItem
import watch.craft.dsl.*
import watch.craft.utils.mapper
import java.net.URI

class AffinityScraper : Scraper {
  private val mapper = mapper()

  override val root = fromJsonRoots(JSON_ROOT) { content: Any ->
    val idx = mapper.convertValue<Index>(content)

    idx.products
      .map { p ->
        fromJson(p.name, (JSON_ROOT.url.toString() + "/" + p.url).toUri()) { leaf ->
          val product = mapper.convertValue<Product>(leaf)
          val desc = Jsoup.parse(product.description) // HTML nested in JSON :/
          val abv = desc.orSkip("No ABV, so assume not a beer") { abvFrom() }
          val sizeMl = product.name.sizeMlFrom()

          ScrapedItem(
            name = product.name.cleanse(
              "[(].*[)]",
              "\\d+ml",
              "\\d+ litre",
              "keg"
            ),
            desc = desc.textFrom(),
            abv = abv,
            available = true,
            offers = product.variants.map { v ->
              Offer(
                quantity = v.optionsValues.quantity?.maybe { extract("\\d+").intFrom(0) } ?: 1,
                totalPrice = v.price,
                sizeMl = sizeMl,
                format = if (sizeMl >= 1000) KEG else CAN
              )
            }.toSet(),
            thumbnailUrl = product.images.first().url,
            url = (HTML_ROOT.toString() + p.url).toUri()
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
    private val HTML_ROOT = URI("https://affinitybrewco.com/shop.html#!/products/")
  }
}
