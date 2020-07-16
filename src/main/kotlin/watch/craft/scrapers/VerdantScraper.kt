package watch.craft.scrapers

import watch.craft.Offer
import watch.craft.Scraper
import watch.craft.Scraper.Job.Leaf
import watch.craft.Scraper.ScrapedItem
import watch.craft.SkipItemException
import watch.craft.utils.*
import java.net.URI

class VerdantScraper : Scraper {
  override val jobs = forRootUrls(ROOT_URL) { root ->
    root
      .selectMultipleFrom(".collection-products .product")
      .map { el ->
        val title = el.textFrom(".product__title")

        Leaf(title, el.hrefFrom("a.product__img-wrapper")) { doc ->
          if (BLACKLIST.any { title.containsWord(it) }) {
            throw SkipItemException("Assuming not a beer")
          }
          val mixed = title.contains("mixed", ignoreCase = true)
          val subtitle = doc.textFrom(".product__subtitle")
          val titleParts = title.extract("([^\\d]+)\\s+(\\d+) pack")

          ScrapedItem(
            name = titleParts.stringFrom(1),
            summary = subtitle.split("—")[0].trim(),
            desc = doc.formattedTextFrom(".product__desc"),
            mixed = mixed,
            abv = if (mixed) null else subtitle.abvFrom(),
            available = ".product__stock--out-of-stock" !in el,
            offers = setOf(
              Offer(
                quantity = titleParts.intFrom(2),
                totalPrice = doc.priceFrom(".product__price"),
                sizeMl = doc.sizeMlFrom(".product__volume")
              )
            ),
            thumbnailUrl = el.srcFrom("img.product__img")
          )
        }
      }
  }

  companion object {
    private val ROOT_URL = URI("https://verdantbrewing.co/collections/beer-merchandise")

    private val BLACKLIST = listOf("glass", "tee", "gift")
  }
}
