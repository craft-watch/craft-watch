package watch.craft.scrapers

import watch.craft.Offer
import watch.craft.Scraper
import watch.craft.Scraper.Job.Leaf
import watch.craft.Scraper.ScrapedItem
import watch.craft.dsl.*
import java.net.URI

class BurningSkyScraper : Scraper {
  override val jobs = forRootUrls(*ROOT_URLS) { root ->
    root
      .selectMultipleFrom(".products .item")
      .map { el ->
        val title = el.textFrom(".product-title")

        Leaf(title, el.urlFrom(".woocommerce-LoopProduct-link")) { doc ->

          ScrapedItem(
            name = title.split(" - ")[0],
            summary = null,
            desc = doc.formattedTextFrom(".summary p"),   // Only take the first paragraph
            abv = title.abvFrom(),
            available = ".out-of-stock" !in el,
            offers = setOf(
              Offer(
                quantity = 1,
                totalPrice = el.priceFrom(".price"),
                sizeMl = doc.sizeMlFrom(".custom-attributes"),
                format = doc.formatFrom(".product_meta")
              )
            ),
            thumbnailUrl = doc.urlFrom(".woocommerce-product-gallery__image img")
          )
        }
      }
  }

  companion object {
    private val ROOT_URLS = arrayOf(
      URI("https://www.burningskybeer.com/bottles/"),
      URI("https://www.burningskybeer.com/cans/")
    )
  }
}
