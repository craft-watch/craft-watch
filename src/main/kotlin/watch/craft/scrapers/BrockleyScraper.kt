package watch.craft.scrapers

import watch.craft.Format.*
import watch.craft.Offer
import watch.craft.Scraper

import watch.craft.Scraper.Node.ScrapedItem
import watch.craft.SkipItemException
import watch.craft.dsl.*

class BrockleyScraper : Scraper {
  override val root = forRoots(*ROOTS) { root, format ->
    root
      .selectMultipleFrom("product-item-root".hook())
      .map { el ->
        val title = el.textFrom("product-item-name".hook())

        work(title, el.urlFrom("product-item-container".hook())) { doc ->
          if (title.containsWord(*BLACKLIST.toTypedArray())) {
            throw SkipItemException("Can't deal with this")
          }

          val desc = doc.formattedTextFrom("description".hook())

          ScrapedItem(
            name = title.cleanse(
              "\\d+(ml|L)",
              "x\\s*\\d+",
              "mini keg",
              "of\\s+\\d+\\s+bottles"
            ).toTitleCase(),
            summary = el.maybe { textFrom("product-item-ribbon".hook()) },
            desc = desc,
            mixed = desc.containsWord("mix", "mixed"),
            abv = desc.maybe { abvFrom() },
            available = "product-item-out-of-stock".hook() !in el,
            offers = setOf(
              Offer(
                quantity = if (format == KEG) 1 else title.quantityFrom("case of (\\d+)"),
                totalPrice = doc.priceFrom("formatted-primary-price".hook()),
                sizeMl = title.maybe { sizeMlFrom() },
                format = format
              )
            ),
            thumbnailUrl = el.attrFrom("product-item-images".hook(), "style")
              .extract("background-image:url[(](.*)[)]").stringFrom(1)
              .toUri()
          )
        }
      }
  }

  private fun String.hook() = "[data-hook=${this}]"

  companion object {
    private val ROOTS = arrayOf(
      root("https://www.brockleybrewery.co.uk/online-shop?ALL%20PRODUCTS=BOTTLES", BOTTLE),
      root("https://www.brockleybrewery.co.uk/online-shop?ALL%20PRODUCTS=CANS", CAN),
      root("https://www.brockleybrewery.co.uk/online-shop?ALL%20PRODUCTS=5L%20MINI%20KEG%20%26%20CASK", KEG)
    )

    private val BLACKLIST = listOf("pints")
  }
}
