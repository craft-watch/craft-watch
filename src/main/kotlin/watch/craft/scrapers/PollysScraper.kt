package watch.craft.scrapers

import watch.craft.Format.CAN
import watch.craft.Offer
import watch.craft.Scraper
import watch.craft.Scraper.Job.Leaf
import watch.craft.Scraper.ScrapedItem
import watch.craft.SkipItemException
import watch.craft.dsl.*

class PollysScraper : Scraper {
  override val jobs = forRoots(ROOT) { root ->
    root
      .selectMultipleFrom(".product")
      .map { el ->
        val rawName = el.textFrom(".woocommerce-loop-product__title")
        val a = el.selectFrom(".woocommerce-loop-product__link")

        Leaf(rawName, a.urlFrom()) { doc ->
          if (rawName.containsWord(*BLACKLIST.toTypedArray())) {
            throw SkipItemException("Not something we can deal with")
          }

          val parts = rawName.extract("(.*) – (.*) (\\S+%)")

          ScrapedItem(
            name = parts.stringFrom(1),
            summary = parts.stringFrom(2),
            desc = doc.formattedTextFrom("#tab-description"),
            mixed = false,
            abv = rawName.abvFrom(),
            available = ".out-of-stock" !in doc,
            offers = setOf(
              Offer(
                totalPrice = doc.priceFrom("#main .woocommerce-Price-amount"),
                sizeMl = POLLYS_CAN_SIZE_ML,
                format = CAN
              )
            ),
            thumbnailUrl = a.urlFrom("img")
          )
        }
      }
  }

  companion object {
    private val ROOT = root("https://shop.pollysbrew.co/")

    private const val POLLYS_CAN_SIZE_ML = 440

    private val BLACKLIST = listOf("mix", "glass")
  }
}
