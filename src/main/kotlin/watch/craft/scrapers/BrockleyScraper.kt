package watch.craft.scrapers

import watch.craft.Offer
import watch.craft.Scraper
import watch.craft.Scraper.Job.Leaf
import watch.craft.Scraper.ScrapedItem
import watch.craft.utils.*
import java.net.URI

class BrockleyScraper : Scraper {
  override val jobs = forRootUrls(*ROOT_URLS) { root ->
    root
      .selectMultipleFrom("product-item-root".hook())
      .map { el ->
        val title = el.textFrom("product-item-name".hook())

        Leaf(title, el.urlFrom("product-item-container".hook())) { doc ->

          val desc = doc.formattedTextFrom("description".hook())

          ScrapedItem(
            name = title.cleanse("\\d+ml", "x\\s*\\d+").toTitleCase(),
            summary = el.maybe { textFrom("product-item-ribbon".hook()) },
            desc = desc,
            mixed = desc.containsWord("mix", "mixed"),
            abv = desc.maybe { abvFrom() },
            available = "product-item-out-of-stock".hook() !in el,
            offers = setOf(
              Offer(
                quantity = 1,
                totalPrice = doc.priceFrom("formatted-primary-price".hook()),
                sizeMl = title.maybe { sizeMlFrom() },
                format = null   // TODO - introduce "context" for rootUrls
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
    private val ROOT_URLS = arrayOf(
      URI("https://www.brockleybrewery.co.uk/online-shop?ALL%20PRODUCTS=BOTTLES"),
      URI("https://www.brockleybrewery.co.uk/online-shop?ALL%20PRODUCTS=CANS")
      // TODO - other pages
    )
  }
}
