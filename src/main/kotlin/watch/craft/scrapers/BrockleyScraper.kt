package watch.craft.scrapers

import org.jsoup.nodes.Document
import watch.craft.Format
import watch.craft.Offer
import watch.craft.Scraper
import watch.craft.Scraper.Job.Leaf
import watch.craft.Scraper.ScrapedItem
import watch.craft.utils.*
import java.net.URI

class BrockleyScraper : Scraper {
  override val jobs = forRootUrls(*ROOT_URLS) { root, format ->
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
                quantity = title.quantityFrom(),
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
    private val ROOT_URLS = arrayOf(
      UrlAndContext("https://www.brockleybrewery.co.uk/online-shop?ALL%20PRODUCTS=BOTTLES", Format.BOTTLE),
      UrlAndContext("https://www.brockleybrewery.co.uk/online-shop?ALL%20PRODUCTS=CANS", Format.CAN)
      // TODO - other pages
    )
  }
}
