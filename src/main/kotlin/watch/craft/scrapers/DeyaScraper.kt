package watch.craft.scrapers

import watch.craft.Offer
import watch.craft.Scraper

import watch.craft.Scraper.Node.ScrapedItem
import watch.craft.dsl.*

class DeyaScraper : Scraper {
  override val roots = fromHtmlRoots(ROOT) { root ->
    root()
      .selectMultipleFrom(".products .product")
      .map { el ->
        val title = el.textFrom(".woocommerce-loop-product__title")

        fromHtml(title, el.urlFrom(".woocommerce-LoopProduct-link")) { doc ->
          val desc = doc().formattedTextFrom(".woocommerce-product-details__short-description")
          val mixed = title.containsMatch("mix")

          ScrapedItem(
            name = title.cleanse(" / \\d+ pack"),
            summary = null,
            desc = desc,
            mixed = mixed,
            abv = if (mixed) null else desc.abvFrom(),
            available = true,
            offers = setOf(
              Offer(
                quantity = title.quantityFrom(),
                totalPrice = el.priceFrom(".price"),
                sizeMl = desc.sizeMlFrom(),
                format = desc.formatFrom()
              )
            ),
            thumbnailUrl = el.urlFrom(".attachment-woocommerce_thumbnail")
          )
        }
      }
  }

  companion object {
    private val ROOT = root("https://shop.deyabrewing.com/product-category/beer/")
  }
}
