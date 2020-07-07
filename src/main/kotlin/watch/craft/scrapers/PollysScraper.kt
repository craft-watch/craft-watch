package watch.craft.scrapers

import watch.craft.Brewery
import watch.craft.Scraper
import watch.craft.Scraper.Job.Leaf
import watch.craft.Scraper.ScrapedItem
import watch.craft.SkipItemException
import watch.craft.utils.*
import java.net.URI

class PollysScraper : Scraper {
  override val brewery = Brewery(
    shortName = "Polly's Brew",
    name = "Polly's Brew Co",
    location = "Mold, Flintshire",
    websiteUrl = URI("https://pollysbrew.co/")
  )

  override val jobs = forRootUrls(ROOT_URL) { root ->
    root
      .selectMultipleFrom(".product")
      .map { el ->
        val rawName = el.textFrom(".woocommerce-loop-product__title")
        val a = el.selectFrom(".woocommerce-loop-product__link")

        Leaf(rawName, a.hrefFrom()) { doc ->
          if (rawName.contains("mix", ignoreCase = true)) {
            throw SkipItemException("Don't know how to deal with mixed packs")
          }

          val parts = rawName.extract("(.+?) – (.+?) – (\\d+(\\.\\d+)?)")

          ScrapedItem(
            name = parts[1],
            summary = parts[2],
            desc = doc.formattedTextFrom("#tab-description"),
            mixed = false,
            sizeMl = POLLYS_CAN_SIZE_ML,
            abv = parts[3].toDouble(),
            available = ".out-of-stock" !in doc,
            numItems = 1,
            price = doc.priceFrom("#main .woocommerce-Price-amount"),
            thumbnailUrl = a.srcFrom("img")
          )
        }
      }
  }

  companion object {
    private val ROOT_URL = URI("https://shop.pollysbrew.co/")

    const val POLLYS_CAN_SIZE_ML = 440
  }
}
