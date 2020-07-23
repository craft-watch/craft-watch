package watch.craft.scrapers

import org.jsoup.nodes.Document
import watch.craft.Format.CAN
import watch.craft.Offer
import watch.craft.Scraper
import watch.craft.Scraper.Job.Leaf
import watch.craft.Scraper.ScrapedItem
import watch.craft.dsl.*
import watch.craft.jsonld.Thing.Product
import watch.craft.jsonld.jsonLdFrom
import watch.craft.shopify.shopifyItems
import java.net.URI

class VillagesScraper : Scraper {
  override val jobs = forRootUrls(ROOT_URL) { root ->
    root
      .shopifyItems()
      .map { details ->
        Leaf(details.title, details.url) { doc ->
          val parts = doc.extractVariableParts(details.title)

          ScrapedItem(
            name = parts.name.toTitleCase(),
            summary = parts.summary,
            desc = doc.maybe { formattedTextFrom(".product-single__description") },
            mixed = parts.mixed,
            abv = parts.abv,
            available = details.available,
            offers = parts.offers,
            thumbnailUrl = details.thumbnailUrl
          )
        }
      }
  }

  private data class VariableParts(
    val name: String,
    val summary: String? = null,
    val mixed: Boolean = false,
    val abv: Double? = null,
    val offers: Set<Offer>
  )

  private fun Document.extractVariableParts(title: String): VariableParts {
    val sizeMl = maybe { sizeMlFrom() }
    val product = jsonLdFrom<Product>().single()

    return if (title.containsMatch("mixed case")) {
      val parts = title.extract("^(.*?) \\((.*)\\)$")
      VariableParts(
        name = parts[1],
        mixed = true,
        offers = setOf(
          Offer(
            quantity = 24,    // Hard-coded
            totalPrice = product.offers.single().price,
            sizeMl = sizeMl,
            format = CAN
          )
        )
      )
    } else {
      val parts = title.extract("^([^ ]*) (.*)? .*%")
      VariableParts(
        name = parts[1],
        summary = parts[2],
        abv = title.abvFrom(),
        offers = product.offers.map {
          Offer(
            quantity = it.sku!!.extract("\\d+").intFrom(0),
            totalPrice = it.price,
            sizeMl = sizeMl,
            format = CAN
          )
        }.toSet()
      )
    }
  }

  companion object {
    private val ROOT_URL = URI("https://villagesbrewery.com/collections/buy-beer")
  }
}
