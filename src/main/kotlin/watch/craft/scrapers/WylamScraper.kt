package watch.craft.scrapers

import watch.craft.*
import watch.craft.Scraper.Job.Leaf
import watch.craft.Scraper.ScrapedItem
import java.net.URI

class WylamScraper : Scraper {
  override val brewery = Brewery(
    shortName = "Wylam",
    name = "Wylam Brewery",
    location = "Newcastle upon Tyne",
    websiteUrl = URI("https://www.wylambrewery.co.uk/")
  )

  override val jobs = forRootUrls(ROOT_URL) { root ->
    root
      .selectMultipleFrom(".ec-grid .grid-product")
      .map { el ->
        val a = el.selectFrom(".grid-product__title")
        val rawName = a.text()

        Leaf(rawName, a.hrefFrom()) { doc ->
          val data = doc.jsonFrom<Data>("script[type=application/ld+json]:not(.yoast-schema-graph)")

          val abvParts = rawName.maybeExtract(ABV_REGEX)
          val servingParts = rawName.maybeExtract("${INT_REGEX}\\s*x")

          if (abvParts == null || servingParts == null) {
            throw SkipItemException("Couldn't extract all parts, so assume it's not a beer")
          }

          val nameParts = rawName.extract("^([^(|]*)\\s*(?:\\((.*)\\))?")

          ScrapedItem(
            name = nameParts[1].trim(),
            summary = nameParts[2].trim().ifBlank { null },
            desc = doc.formattedTextFrom(".product-details__product-description"),
            sizeMl = rawName.sizeMlFrom(),
            abv = abvParts[1].toDouble(),
            available = data.offers.availability == "http://schema.org/InStock",
            numItems = servingParts[1].toInt(),
            price = data.offers.price,
            thumbnailUrl = el.srcFrom("img.grid-product__picture")
          )
        }
      }
  }

  private data class Data(
    val description: String,
    val image: List<URI>,
    val offers: Offer
  ) {
    data class Offer(
      val price: Double,
      val availability: String
    )
  }

  companion object {
    private val ROOT_URL = URI("https://www.wylambrewery.co.uk/beer-store/")
  }
}
