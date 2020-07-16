package watch.craft.scrapers

import watch.craft.Offer
import watch.craft.Scraper
import watch.craft.Scraper.Job.Leaf
import watch.craft.Scraper.ScrapedItem
import watch.craft.SkipItemException
import watch.craft.utils.*
import java.net.URI
import kotlin.text.RegexOption.IGNORE_CASE

class NorthernMonkScraper : Scraper {
  override val jobs = forPaginatedRootUrl(ROOT_URL) { root ->
    root
      .selectMultipleFrom(".card")
      .map { el ->
        val rawName = el.textFrom(".card__name").toTitleCase()

        Leaf(rawName, el.urlFrom(".card__wrapper")) { doc ->
          val desc = doc.selectFrom(".product__description")
          val abv = desc.maybe { abvFrom() }
          val mixed = desc.children()
            .count { it.text().contains(ITEM_MULTIPLE_REGEX.toRegex(IGNORE_CASE)) } > 1

          if (abv == null && !mixed) {
            throw SkipItemException("Assume that lack of ABV for non-mixed means not a beer product")
          }

          val nameParts = rawName
            .replace(PACK_REGEX.toRegex(IGNORE_CASE), "")
            .split("//")[0]
            .split("™")
            .map { it.trim() }

          val data = doc.jsonFrom<Data>("script[type=application/json][data-product-json]")

          ScrapedItem(
            name = nameParts[0],
            summary = if (nameParts.size > 1) {
              nameParts[1]
            } else {
              rawName.maybe { extract("[^/]+\\s+//\\s+(.*)")[1] }
            },
            desc = desc.formattedTextFrom(),
            mixed = mixed,
            abv = abv,
            available = data.available,
            offers = setOf(
              Offer(
                quantity = rawName.maybe { extract(PACK_REGEX).intFrom(1) } ?: 1,
                totalPrice = data.price / 100.0,
                sizeMl = desc.maybe { sizeMlFrom() }
              )
            ),
            thumbnailUrl = URI(
              // The URLs are dynamically created
              doc.attrFrom(".product__image.lazyload", "abs:data-src")
                .replace("{width}", "180")
            )
          )
        }
      }
  }

  private data class Data(
    val available: Boolean,
    val price: Int
  )

  companion object {
    private val ROOT_URL = URI("https://northernmonkshop.com/collections/beer")

    private const val PACK_REGEX = "(\\d+) pack"
    private const val ITEM_MULTIPLE_REGEX = "\\d+\\s+x"
  }
}
