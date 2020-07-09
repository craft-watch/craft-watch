package watch.craft.scrapers

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonProperty
import org.jsoup.nodes.Document
import watch.craft.Brewery
import watch.craft.Scraper
import watch.craft.Scraper.Job.Leaf
import watch.craft.Scraper.ScrapedItem
import watch.craft.SkipItemException
import watch.craft.utils.*
import java.net.URI

class RedWillowScraper : Scraper {
  override val brewery = Brewery(
    shortName = "RedWillow",
    name = "RedWillow Brewery",
    location = "Macclesfield, Cheshire",
    websiteUrl = URI("https://www.redwillowbrewery.com/")
  )

  // TODO - extract summary

  override val jobs = forRootUrls(ROOT_URL) { root ->
    root
      .selectMultipleFrom(".ProductList-grid .ProductList-item")
      .map { el ->
        val rawName = el.textFrom(".ProductList-title")

        Leaf(rawName, el.hrefFrom("a.ProductList-item-link")) { doc ->
          val bestDeal = doc.extractBestDeal()

          val desc = doc.formattedTextFrom(".ProductItem-details-excerpt")
          val allText = rawName + "\n" + desc

          if (BLACKLIST.any { rawName.contains(it, ignoreCase = true) }) {
            throw SkipItemException("Identified as non-beer")
          }

          ScrapedItem(
            name = rawName.extract("^[A-Za-z-'\\s]+")[0].trim().toTitleCase(),
            summary = rawName.maybe { extract("%\\s+([^\\d]+)$")[1] },  // In some cases, beer type is after the ABV
            desc = desc.ifBlank { null },
            mixed = rawName.contains("mixed", ignoreCase = true),
            sizeMl = allText.maybe { sizeMlFrom() },
            abv = rawName.maybe { abvFrom() },
            available = true,
            quantity = bestDeal.numItems,
            totalPrice = bestDeal.price,
            thumbnailUrl = doc.extractSmallThumbnail()
          )
        }
      }
  }

  // By default, the images are huge!
  private fun Document.extractSmallThumbnail() = (
    dataSrcFrom("img.ProductItem-gallery-slides-item-image").toString() + "?format=200w"
    ).toUri()

  private fun Document.extractBestDeal() = maybe { attrFrom(".product-variants", "data-variants") }
    ?.parseJson<List<Variant>>()
    ?.map { Deal(price = it.price.toDouble() / 100, numItems = it.attributes.quantity) }
    ?.minBy { it.price / it.numItems }  // TODO - prefer minimum # items
    ?: Deal(
      price = priceFrom(".product-price"),
      numItems = extractFrom(".ProductItem-details-title", "(\\d+)\\s*x").intFrom(1)
    ) // Fallback to assuming a single price

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
      @JsonAlias("Quanity")   // Sigh
      val quantity: Int
    )
  }

  companion object {
    private val ROOT_URL = URI("https://www.redwillowbrewery.com/shop")

    private val BLACKLIST = listOf("glassware")
  }
}
