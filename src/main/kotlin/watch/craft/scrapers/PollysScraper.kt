package watch.craft.scrapers

import org.jsoup.nodes.Document
import watch.craft.*
import watch.craft.Scraper.IndexEntry
import watch.craft.Scraper.ScrapedItem
import java.net.URI

class PollysScraper : Scraper {
  override val name = "Polly's Brew"
  override val rootUrls = listOf(URI("https://shop.pollysbrew.co/"))

  override fun scrapeIndex(root: Document) = root
    .selectMultipleFrom(".product")
    .map { el ->
      val rawName = el.textFrom(".woocommerce-loop-product__title")
      val a = el.selectFrom(".woocommerce-loop-product__link")

      IndexEntry(rawName, a.hrefFrom()) { doc ->
        if (rawName.contains("mix", ignoreCase = true)) {
          throw SkipItemException("Don't know how to deal with mixed packs")
        }

        val parts = rawName.extract("(.+?) – (.+?) – (\\d+(\\.\\d+)?)")

        ScrapedItem(
          name = parts[1],
          summary = parts[2],
          desc = doc.normaliseParagraphsFrom("#tab-description"),
          mixed = false,
          sizeMl = POLLYS_CAN_SIZE_ML,
          abv = parts[3].toDouble(),
          available = doc.maybeSelectFrom(".out-of-stock") == null,
          numItems = 1,
          price = doc.priceFrom("#main .woocommerce-Price-amount"),
          thumbnailUrl = a.srcFrom("img")
        )
      }
    }

  companion object {
    const val POLLYS_CAN_SIZE_ML = 440
  }
}
