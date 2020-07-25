package watch.craft.scrapers

import watch.craft.Offer
import watch.craft.Scraper

import watch.craft.Scraper.Output.ScrapedItem
import watch.craft.dsl.*

class ThornbridgeScraper : Scraper {
  override val seed = forRoots(ROOT) { root ->
    root
      .selectMultipleFrom(".grid-uniform > .grid-item")
      .map { el ->
        val rawName = el.textFrom(".h6")

        work(rawName, el.urlFrom("a")) { doc ->
          val abv = orSkip("No ABV in title, so assume it's not a beer") { rawName.abvFrom() }

          val parts = rawName.extract("(.*?)\\W+\\d.*%\\W+(.*)")
          val desc = doc.selectFrom(".product-description")

          // TODO - identify mixed packs

          ScrapedItem(
            thumbnailUrl = doc.urlFrom(".product__image-wrapper img"),
            name = parts[1].cleanse(" (bottle|can)$"),
            summary = parts[2],
            desc = desc.formattedTextFrom(),
            mixed = false,
            abv = abv,
            available = "sold-out" !in el.classNames(),
            offers = setOf(
              Offer(
                quantity = desc.maybe { quantityFrom() } ?: rawName.maybe { quantityFrom() } ?: 1,
                totalPrice = el.priceFrom(".product-item--price"),
                sizeMl = desc.maybe { sizeMlFrom() }
              )
            )
          )
        }
      }
  }

  companion object {
    // TODO - URI("https://shop.thornbridgebrewery.co.uk/collections/smart-collection?view=list")
    private val ROOT = root("https://shop.thornbridgebrewery.co.uk/collections/pick-and-mix-beers?view=list")
  }
}
