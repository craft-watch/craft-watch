package watch.craft.scrapers

import watch.craft.Offer
import watch.craft.Scraper

import watch.craft.Scraper.Node.ScrapedItem
import watch.craft.dsl.*

class JeffersonsScraper : Scraper {
  override val roots = fromHtmlRoots(ROOT) { root ->
    root
      .selectMultipleFrom(".grid--view-items .grid__item")
      .map { el ->
        val rawName = el.textFrom(".grid-view-item__title")

        fromHtml(rawName, el.urlFrom(".grid-view-item__link")) { doc ->
          val desc = doc.formattedTextFrom(".product-single__description")
          val abv = desc.orSkip("No ABV found, so assume not a beer") { abvFrom() }
          val parts = rawName.extract("(.*?)\\s*-\\s*([\\w\\s]+)")

          ScrapedItem(
            name = parts.stringFrom(1),
            summary = parts.stringFrom(2),
            desc = desc,
            abv = abv,
            available = true,
            offers = setOf(
              Offer(
                quantity = desc.quantityFrom(),
                totalPrice = el.priceFrom(".product-price__price"),
                sizeMl = desc.sizeMlFrom()
              )
            ),
            thumbnailUrl = el.urlFrom(".grid-view-item__image")
          )
        }
      }
  }

  companion object {
    private val ROOT = root("https://jeffersonsbrewery.co.uk/")
  }
}
