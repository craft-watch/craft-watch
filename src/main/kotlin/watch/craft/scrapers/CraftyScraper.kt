package watch.craft.scrapers

import org.jsoup.nodes.Element
import watch.craft.Offer
import watch.craft.Scraper
import watch.craft.Scraper.Job.Leaf
import watch.craft.Scraper.ScrapedItem
import watch.craft.SkipItemException
import watch.craft.shopify.shopifyItems
import watch.craft.utils.*
import java.net.URI
import kotlin.text.RegexOption.IGNORE_CASE

class CraftyScraper : Scraper {
  override val jobs = forPaginatedRootUrl(ROOT_URL) { root ->
    root
      .selectMultipleFrom(".product")
      .map { el ->
        val title = el.textFrom(".woocommerce-loop-product__title")

        Leaf(title, el.hrefFrom(".woocommerce-LoopProduct-link")) { doc ->
          val mixed = doc.textFrom(".product_meta").contains("mixed", ignoreCase = true)
          val desc = doc.formattedTextFrom(".et_pb_wc_description")


          ScrapedItem(
            name = title
              .replace("[(].*[)]".toRegex(), "")
              .replace("\\s–\\s.*".toRegex(), "")
              .trim(),
            summary = null,
            desc = desc,
            mixed = mixed,
            abv = if (mixed) null else desc.abvFrom(),
            available = ".woosticker_sold" !in el,
            offers = setOf(
              Offer(
                quantity = 1,
                totalPrice = 0.00,
                sizeMl = desc.sizeMlFrom(),
                format = desc.formatFrom()
              )
            ),
            thumbnailUrl = el.srcFrom(".attachment-woocommerce_thumbnail")
          )
        }
      }
  }

  private fun Element.extractAttributes() = selectMultipleFrom("tr")
    .associate { it.textFrom("td:first-child").toLowerCase() to it.textFrom("td:last-child") }

  companion object {
    val ROOT_URL = URI("https://www.craftybrewing.co.uk/crafty-core-beers/")
  }
}
