package watch.craft.scrapers

import watch.craft.Offer
import watch.craft.Scraper
import watch.craft.Scraper.Node.ScrapedItem
import watch.craft.SkipItemException
import watch.craft.dsl.*
import watch.craft.jsonld.Thing.Product
import watch.craft.jsonld.jsonLdFrom

class ForestRoadScraper : Scraper {
  override val roots = fromHtmlRoots(*ROOTS) { root ->
    root()
      .selectMultipleFrom("#Collection .grid__item")
      .map { el ->
        val title = el.textFrom(".grid-view-item__title")

        fromHtml(title, el.urlFrom("a")) { doc ->
          if (title.containsMatch("subscription")) {
            throw SkipItemException("Subscriptions aren't something we can model")
          }

          val desc = doc().formattedTextFrom(".product-single__description")
          val sizeMl = maybeAnyOf(
            { desc.sizeMlFrom() },
            { title.sizeMlFrom() }
          )
          val mixed = title.containsMatch("mixed")

          ScrapedItem(
            name = title.cleanse(
              " - .*",
              "[(].*[)]",
              "\\d+ pack"
            ).toTitleCase(),
            desc = desc,
            mixed = mixed,
            abv = if (mixed) null else desc.abvFrom(),
            available = ".product-price__sold-out" !in el,
            offers = doc().jsonLdFrom<Product>().single()
              .offers
              .map { offer ->
                Offer(
                  quantity = offer.itemOffered!!.name.maybe { quantityFrom() }
                    ?: title.maybe { quantityFrom() }
                    ?: 1,
                  totalPrice = offer.price,
                  sizeMl = sizeMl,
                  format = title.formatFrom(fullProse = false)
                )
              }
              .toSet(),
            thumbnailUrl = el.urlFrom("img")
          )
        }
      }
  }

  companion object {
    private val ROOTS = arrayOf(
      root("https://forest-road.myshopify.com/collections/frontpage")
    )
  }
}
