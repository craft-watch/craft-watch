package watch.craft.scrapers

import watch.craft.Offer
import watch.craft.Scraper
import watch.craft.Scraper.Node.ScrapedItem
import watch.craft.dsl.*

class VerdantScraper : Scraper {
  override val roots = fromHtmlRoots(ROOT) { root ->
    root()
      .selectMultipleFrom(".collection-products .product")
      .map { el ->
        val title = el.textFrom(".product__title")

        fromHtml(title, el.urlFrom("a.product__img-wrapper")) { doc ->
          title.skipIfOnBlacklist(*BLACKLIST)

          val mixed = title.containsMatch("mixed")
          val subtitle = doc().textFrom(".product__subtitle")
          val titleParts = title.extract("([^\\d]+)\\s+(\\d+) pack")

          ScrapedItem(
            name = titleParts.stringFrom(1),
            summary = subtitle.split("—")[0].trim(),
            desc = doc().formattedTextFrom(".product__desc"),
            mixed = mixed,
            abv = if (mixed) null else subtitle.abvFrom(),
            available = ".product__stock--out-of-stock" !in el,
            offers = setOf(
              Offer(
                quantity = titleParts.intFrom(2),
                totalPrice = doc().priceFrom(".product__price"),
                sizeMl = doc().sizeMlFrom(".product__volume")
              )
            ),
            thumbnailUrl = el.urlFrom("img.product__img", "src")
          )
        }
      }
  }

  companion object {
    private val ROOT = root("https://verdantbrewing.co/collections/beer-merchandise")

    private val BLACKLIST = arrayOf("glass", "tee", "gift")
  }
}
