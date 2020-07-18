package watch.craft.scrapers

import watch.craft.Offer
import watch.craft.Scraper
import watch.craft.Scraper.Job.Leaf
import watch.craft.Scraper.ScrapedItem
import watch.craft.dsl.*
import java.net.URI

class BeakScraper : Scraper {
  override val jobs = forRootUrls(ROOT_URL) { root ->
    root
      .selectMultipleFrom(".collection .product_thumb")
      .map { el ->
        val a = el.selectFrom("a")
        val rawName = a.textFrom("p")

        Leaf(rawName, a.urlFrom()) { doc ->
          val desc = doc.selectFrom(".product_description")
          val allTheText = "${rawName}\n${desc.text()}"

          ScrapedItem(
            name = rawName.split(" ")[0].toTitleCase(),
            summary = null,
            desc = desc.formattedTextFrom(),
            abv = allTheText.abvFrom(),
            available = !a.text().containsMatch("sold out"),
            offers = setOf(
              Offer(
                quantity = allTheText.maybe { quantityFrom() } ?: 1,
                totalPrice = a.priceFrom(".price"),
                sizeMl = allTheText.sizeMlFrom()
              )
            ),
            thumbnailUrl = a.urlFrom("img")
          )
        }
      }
  }

  companion object {
    private val ROOT_URL = URI("https://beakbrewery.com/collections/beer")
  }
}
