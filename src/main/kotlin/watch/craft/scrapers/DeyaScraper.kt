package watch.craft.scrapers

import watch.craft.Offer
import watch.craft.Scraper
import watch.craft.Scraper.Job.Leaf
import watch.craft.Scraper.ScrapedItem
import watch.craft.utils.*
import java.net.URI
import kotlin.text.RegexOption.IGNORE_CASE

class DeyaScraper : Scraper {
  override val jobs = forRootUrls(ROOT_URL) { root ->
    root
      .selectMultipleFrom(".products .product")
      .map { el ->
        val title = el.textFrom(".woocommerce-loop-product__title")

        Leaf(title, el.hrefFrom(".woocommerce-LoopProduct-link")) { doc ->
          val desc = doc.formattedTextFrom(".woocommerce-product-details__short-description")
          val mixed = title.contains("mix", ignoreCase = true)

          ScrapedItem(
            name = title.replace(" / \\d+ pack".toRegex(IGNORE_CASE), ""),
            summary = null,
            desc = desc,
            mixed = mixed,
            abv = if (mixed) null else desc.abvFrom(),
            available = true,
            offers = setOf(
              Offer(
                quantity = title.extract("(\\d+) pack").intFrom(1),
                totalPrice = el.priceFrom(".price"),
                sizeMl = desc.sizeMlFrom(),
                format = desc.formatFrom()
              )
            ),
            thumbnailUrl = el.srcFrom(".attachment-woocommerce_thumbnail")
          )
        }
      }
  }

  companion object {
    val ROOT_URL = URI("https://shop.deyabrewing.com/product-category/beer/")
  }
}
