package watch.craft.scrapers

import com.fasterxml.jackson.annotation.JsonProperty
import org.jsoup.nodes.Document
import watch.craft.Brewery
import watch.craft.Scraper
import watch.craft.Scraper.Job.Leaf
import watch.craft.Scraper.ScrapedItem
import watch.craft.utils.*
import java.net.URI

class RedWillowScraper : Scraper {
  override val brewery = Brewery(
    shortName = "RedWillow",
    name = "RedWillow Brewery",
    location = "Macclesfield, Cheshire",
    websiteUrl = URI("https://www.redwillowbrewery.com/")
  )

  override val jobs = forRootUrls(ROOT_URL) { root ->
    root
      .selectMultipleFrom(".ProductList-grid .ProductList-item")
      .map { el ->
        val rawName = el.textFrom(".ProductList-title")

        Leaf(rawName, el.hrefFrom("a.ProductList-item-link")) { doc ->
//          val data = doc
//            .selectMultipleFrom("script[type=application/ld+json]")
//            .map { it.jsonFrom<Map<String, Any>>() }
//            .forEach { println(mapper().writeValueAsString(it)) }


          doc.maybe { attrFrom(".product-variants", "data-variants") }

          val bestDeal = doc.extractBestDeal()

//          val parts = rawName.extract("([^0-9]+)\\s+(\\d(\\.\\d+)?)%")//\\s(.*?)")
//          println(parts)

          ScrapedItem(
            name = rawName.extract("^[A-Za-z-\\s]+")[0].trim().toTitleCase(),
            summary = null,
            desc = doc.formattedTextFrom(".ProductItem-details-excerpt"),
            sizeMl = null,
            abv = rawName.maybe { abvFrom() },
            available = true,
            numItems = bestDeal.numItems,
            price = bestDeal.price,
            thumbnailUrl = el.dataSrcFrom("img.ProductList-image")
          )
        }
      }
  }


  private fun Document.extractBestDeal(): Deal {
    val variants = maybe {
      attrFrom(".product-variants", "data-variants").parseJson<List<Variant>>()
    }

    return if (variants != null) {
      variants
        .map { Deal(price = it.price.toDouble() / 100, numItems = it.attributes.quantity) }
        .minBy { it.price / it.numItems }!!
    } else {
      Deal(
        price = priceFrom(".product-price"),
        numItems = extractFrom(".ProductItem-details-title", "(\\d+)\\s*x").intFrom(1)
      )
    }
  }

  private data class Deal(
    val price: Double,
    val numItems: Int
  )

  private data class Variant(
    val attributes: Attributes,
    val price: Int
  ) {
    data class Attributes(
      @JsonProperty("Quantity")
      val quantity: Int
    )
  }

  companion object {
    private val ROOT_URL = URI("https://www.redwillowbrewery.com/shop")
  }
}
