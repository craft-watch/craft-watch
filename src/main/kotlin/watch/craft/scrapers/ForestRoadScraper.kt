package watch.craft.scrapers

import watch.craft.Brewery
import watch.craft.Format.BOTTLE
import watch.craft.Format.CAN
import watch.craft.Offer
import watch.craft.Scraper
import watch.craft.Scraper.Job.Leaf
import watch.craft.Scraper.ScrapedItem
import watch.craft.SkipItemException
import watch.craft.utils.*
import java.net.URI
import kotlin.text.RegexOption.IGNORE_CASE

class ForestRoadScraper : Scraper {
  override val brewery = Brewery(
    shortName = "Forest Road",
    name = "Forest Road Brewing Co",
    location = "Hackney, London",
    websiteUrl = URI("https://www.forestroad.co.uk/")
  )

  // TODO - specials?  https://www.forestroad.co.uk/shop?category=SPECIAL

  override val jobs = forRootUrls(ROOT_URL) { root ->
    root
      .selectMultipleFrom(".Main--products-list .ProductList-item")
      .map { el ->
        val title = el.textFrom(".ProductList-title")

        Leaf(title, el.hrefFrom("a.ProductList-item-link")) { doc ->
          if (title.contains("subscription", ignoreCase = true)) {
            throw SkipItemException("Subscriptions aren't something we can model")
          }

          val desc = doc.formattedTextFrom(".ProductItem-details-excerpt").toTitleCase()
          val mixed = title.contains("mixed", ignoreCase = true)

          val name = title
            .replace("[(].*[)]".toRegex(), "")
            .replace("cans".toRegex(IGNORE_CASE), "")
            .trim()
            .toTitleCase()

          ScrapedItem(
            name = name,
            summary = desc.split("\n")[0],
            desc = desc,
            mixed = true,
            abv = if (mixed) null else desc.abvFrom(),
            available = true,
            offers = setOf(
              Offer(
                quantity = title.maybe { extract("(\\d+)\\s*x").intFrom(1) } ?: 1,
                totalPrice = el.priceFrom(".product-price"),
                sizeMl = title.sizeMlFrom(),
                format = if (title.contains("cans", ignoreCase = true)) CAN else BOTTLE
              )
            ),
            thumbnailUrl = el.dataSrcFrom("img.ProductList-image")
          )
        }
      }
  }

  companion object {
    val ROOT_URL = URI("https://www.forestroad.co.uk/shop?category=BEER")
  }
}
