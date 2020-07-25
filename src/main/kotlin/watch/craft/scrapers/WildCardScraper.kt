package watch.craft.scrapers

import org.jsoup.nodes.Document
import watch.craft.Format.KEG
import watch.craft.Offer
import watch.craft.Scraper

import watch.craft.Scraper.Node.ScrapedItem
import watch.craft.dsl.*
import watch.craft.jsonld.Thing.Product
import watch.craft.jsonld.jsonLdFrom
import watch.craft.shopify.ShopifyItemDetails
import watch.craft.shopify.shopifyItems

class WildCardScraper : Scraper {
  override val root = fromHtmlRoots(ROOT) { root ->
    root
      .shopifyItems()
      .map { details ->
        fromHtml(details.title, details.url) { doc ->
          val desc = doc.formattedTextFrom(".product-single__description")
          val mixed = details.title.containsWord("mixed", "box")

          ScrapedItem(
            name = details.title
              .cleanse(",.*", "\\d.*", "fresh") // We want to resolve "fresh" and "non-fresh" variants
              .toTitleCase(),
            summary = null,
            desc = desc,
            mixed = mixed,
            abv = if (mixed) null else maybeAnyOf(
              { desc.abvFrom() },
              { details.title.abvFrom() }
            ),
            available = details.available,
            offers = doc.extractOffers(details, desc).toSet(),
            thumbnailUrl = details.thumbnailUrl
          )
        }
      }
  }

  private fun Document.extractOffers(details: ShopifyItemDetails, desc: String): List<Offer> {
    val sizeMl = desc.maybe { sizeMlFrom() }
    val format = desc.formatFrom(disallowed = listOf(KEG))
    val product = jsonLdFrom<Product>().singleOrNull { it.model.isNotEmpty() }

    return if (product != null) {
      product.model.map { model ->
        Offer(
          quantity = model.additionalProperty.find { it.name.contains("Case") }!!.value.toInt(),
          totalPrice = model.offers.single().price,
          sizeMl = sizeMl,
          format = format
        )
      }
    } else {
      listOf(
        Offer(
          quantity = desc.maybe { extract("(\\d+) cans").intFrom(1) } ?: 1,
          totalPrice = details.price,
          sizeMl = sizeMl,
          format = format
        )
      )
    }
  }

  companion object {
    private val ROOT = root("https://shop.wildcardbrewery.co.uk/")
  }
}
