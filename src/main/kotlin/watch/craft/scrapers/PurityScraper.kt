package watch.craft.scrapers

import watch.craft.Offer
import watch.craft.Scraper
import watch.craft.Scraper.Job.Leaf
import watch.craft.Scraper.ScrapedItem
import watch.craft.utils.*
import java.net.URI

class PurityScraper : Scraper {
  override val jobs = forRootUrls(ROOT_URL) { root ->
    root
      .selectMultipleFrom(".product")
      .map { el ->
        val title = el.textFrom(".woocommerce-loop-product__title")

        Leaf(title, el.hrefFrom(".woocommerce-LoopProduct-link")) { doc ->
          val desc = doc.formattedTextFrom(".widget-woocommerce-product-content")

          ScrapedItem(
            name = title,
            summary = null,
            desc = desc,
            abv = desc.abvFrom(),
            available = true,
            offers = setOf(
              Offer(
                quantity = 1,
                totalPrice = 0.00,
                sizeMl = null,
                format = null
              )
            ),
            thumbnailUrl = el.attrFrom(".attachment-woocommerce_thumbnail", "data-lazy-src").toUri()
          )
        }
      }
  }

  companion object {
    private val ROOT_URL = URI("https://puritybrewing.com/product-category/purity/")
  }
}
