package watch.craft.scrapers

import org.jsoup.nodes.Document
import watch.craft.Offer
import watch.craft.Scraper
import watch.craft.Scraper.Job.Leaf
import watch.craft.Scraper.ScrapedItem
import watch.craft.SkipItemException
import watch.craft.dsl.*

class AffinityScraper : Scraper {
  override val jobs = forRoots(ROOT) { root: Document ->
    root
      .selectMultipleFrom(".product")
      .map { el ->
        val title = el.textFrom(".name")

        Leaf(title, el.urlFrom("a")) { doc ->
          ScrapedItem(
            name = title,
            summary = null,
            desc = null,
            abv = null,
            available = true,
            offers = setOf(
              Offer(
                quantity = 1,
                totalPrice = el.priceFrom(".price"),
                sizeMl = null,
                format = null
              )
            ),
            thumbnailUrl = el.urlFrom("img", preference = "src")
          )
        }
      }
  }

  companion object {
    private val ROOT = root("https://www.affinitybrewco.com/shop.html")
  }
}
