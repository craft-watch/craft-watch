package watch.craft.scrapers

import watch.craft.Offer
import watch.craft.Scraper

import watch.craft.Scraper.Node.ScrapedItem
import watch.craft.SkipItemException
import watch.craft.dsl.*
import watch.craft.jsonld.Thing.Product
import watch.craft.jsonld.jsonLdFrom

class WylamScraper : Scraper {
  override val roots = fromHtmlRoots(ROOT) { root, _ ->
    root()
      .selectMultipleFrom(".ec-grid .grid-product")
      .map { el ->
        val a = el.selectFrom(".grid-product__title")
        val rawName = a.text()

        fromHtml(rawName, a.urlFrom()) { doc ->
          val product = doc().jsonLdFrom<Product>().single()
          val abv = rawName.maybe { abvFrom() }
          val numItems = rawName.maybe { quantityFrom() }

          if (abv == null || numItems == null) {
            throw SkipItemException("Couldn't extract all parts, so assume it's not a beer")
          }

          val nameParts = rawName.extract("^([^(|]*)\\s*(?:\\((.*)\\))?")

          ScrapedItem(
            name = nameParts[1].trim(),
            summary = nameParts[2].trim().ifBlank { null },
            desc = doc().formattedTextFrom(".product-details__product-description"),
            abv = abv,
            available = product.offers.single().availability == "http://schema.org/InStock",
            offers = setOf(
              Offer(
                quantity = numItems,
                totalPrice = product.offers.single().price,
                sizeMl = rawName.sizeMlFrom()
              )
            ),
            thumbnailUrl = el.urlFrom("img.grid-product__picture")
          )
        }
      }
  }

  companion object {
    private val ROOT = root("https://www.wylambrewery.co.uk/beer-store/")
  }
}
