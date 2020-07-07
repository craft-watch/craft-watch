package watch.craft.scrapers

import watch.craft.*
import watch.craft.Scraper.Job.Leaf
import watch.craft.Scraper.ScrapedItem
import java.net.URI

class BeakScraper : Scraper {
  override val brewery = Brewery(
    shortName = "Beak",
    name = "Beak Brewery",
    location = "Lewes, East Sussex",
    websiteUrl = URI("https://beakbrewery.com/")
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
            sizeMl = allTheText.sizeMlFrom(),
            abv = allTheText.extract(ABV_REGEX)[1].toDouble(),
            available = !a.text().contains("Sold Out", ignoreCase = true),
            numItems = allTheText.maybe { extract(NUM_ITEMS_REGEX) }?.get(1)?.toInt() ?: 1,
            price = a.priceFrom(".price"),
            thumbnailUrl = a.srcFrom("img")
          )
        }
      }
  }

  companion object {
    const val ML_REGEX = "(\\d+)\\s*ml"
    const val ABV_REGEX = "(\\d+(\\.\\d+)?)\\s*%"
    const val NUM_ITEMS_REGEX = "(\\d+)\\s*x"

    val ROOT_URL = URI("https://beakbrewery.com/collections/beer")
  }
}
