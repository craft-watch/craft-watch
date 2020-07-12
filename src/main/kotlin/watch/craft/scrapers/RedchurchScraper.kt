package watch.craft.scrapers

import watch.craft.Brewery
import watch.craft.Scraper
import watch.craft.Scraper.Job.Leaf
import watch.craft.Scraper.ScrapedItem
import watch.craft.SkipItemException
import watch.craft.shopify.extractShopifyOffers
import watch.craft.utils.*
import java.net.URI

class RedchurchScraper : Scraper {
  override val brewery = Brewery(
    shortName = "Redchurch",
    name = "Redchurch Brewery",
    location = "Harlow, Essex",
    websiteUrl = URI("https://redchurch.beer/")
  )

  override val jobs = forRootUrls(ROOT_URL) { root ->
    root
      .selectMultipleFrom(".product")
      .map { el ->
        val title = el.selectFrom(".product__title")
        val rawName = title.text()

        Leaf(rawName, title.hrefFrom("a")) { doc ->
          val nameParts = rawName.extract(regex = "(Mixed Case - )?(.*)")
          val mixed = !nameParts[1].isBlank()
          val sizeMl = doc.maybe { sizeMlFrom() }
          val abv = doc.maybe { abvFrom() }

          if (!mixed && sizeMl == null && abv == null) {
            throw SkipItemException("Can't identify ABV or size for non-mixed case, so assume it's not a beer")
          }

          ScrapedItem(
            thumbnailUrl = doc.srcFrom(".product-single__photo")
              .toString()
              .replace("\\?.*".toRegex(), "")
              .toUri(),
            name = nameParts[2],
            desc = doc.maybe { formattedTextFrom(".product-single__description") },
            mixed = mixed,
            abv = abv,
            available = ".sold-out-text" !in el,
            offers = doc.orSkip("Don't know how to identify number of items") {
              extractShopifyOffers(sizeMl)
            }
          )
        }
      }
  }

  companion object {
    private val ROOT_URL = URI("https://redchurch.store/")
  }
}
