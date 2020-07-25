package watch.craft.scrapers

import watch.craft.Offer
import watch.craft.Scraper
import watch.craft.Scraper.Node.ScrapedItem
import watch.craft.dsl.*

class BeakScraper : Scraper {
  override val root = fromHtmlRoots(ROOT) { root ->
    root
      .selectMultipleFrom(".collection .product_thumb")
      .map { el ->
        val a = el.selectFrom("a")
        val rawName = a.textFrom("p")

        fromHtml(rawName, a.urlFrom()) { doc ->
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
    private val ROOT = root("https://beakbrewery.com/collections/beer")
  }
}
