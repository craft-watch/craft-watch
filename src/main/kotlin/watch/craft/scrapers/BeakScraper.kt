package watch.craft.scrapers

import watch.craft.Brewery
import watch.craft.Offer
import watch.craft.Scraper
import watch.craft.Scraper.Job.Leaf
import watch.craft.Scraper.ScrapedItem
import watch.craft.utils.*
import java.net.URI

class BeakScraper : Scraper {
  override val brewery = Brewery(
    shortName = "Beak",
    name = "Beak Brewery",
    location = "Lewes, East Sussex",
    websiteUrl = URI("https://beakbrewery.com/"),
    twitterHandle = "TheBeakBrewery"
  )

  override val jobs = forRootUrls(ROOT_URL) { root ->
    root
      .selectMultipleFrom(".collection .product_thumb")
      .map { el ->
        val a = el.selectFrom("a")
        val rawName = a.textFrom("p")

        Leaf(rawName, a.hrefFrom()) { doc ->
          val desc = doc.selectFrom(".product_description")
          val allTheText = "${rawName}\n${desc.text()}"

          ScrapedItem(
            name = rawName.split(" ")[0].toTitleCase(),
            summary = null,
            desc = desc.formattedTextFrom(),
            abv = allTheText.abvFrom(),
            available = !a.text().contains("Sold Out", ignoreCase = true),
            offers = setOf(
              Offer(
                quantity = allTheText.maybe { extract(NUM_ITEMS_REGEX).intFrom(1) } ?: 1,
                totalPrice = a.priceFrom(".price"),
                sizeMl = allTheText.sizeMlFrom()
              )
            ),
            thumbnailUrl = a.srcFrom("img")
          )
        }
      }
  }

  companion object {
    const val NUM_ITEMS_REGEX = "(\\d+)\\s*x"

    val ROOT_URL = URI("https://beakbrewery.com/collections/beer")
  }
}
