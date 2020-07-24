package watch.craft.scrapers

import watch.craft.Offer
import watch.craft.Scraper
import watch.craft.Scraper.Job.Leaf
import watch.craft.Scraper.ScrapedItem
import watch.craft.dsl.*

class InnisAndGunnScraper : Scraper {
  override val jobs = forRoots(ROOT) { root ->
    root
      .selectMultipleFrom(".itemsBrowse .itemWrap")
      .map { el ->
        val a = el.selectFrom("h2 a")
        val rawName = a.textFrom()

        leaf(rawName, a.urlFrom()) { doc ->
          val volume = el.selectFrom(".itemVolume")

          ScrapedItem(
            name = rawName.extract("[^\\d]+").stringFrom(0),
            summary = doc.textFrom(".itemTitle h4"),
            desc = doc.formattedTextFrom(".productDescription .desc"),
            abv = el.abvFrom(".alcoInfo"),
            available = true,
            offers = setOf(
              Offer(
                quantity = volume.maybe { quantityFrom() } ?: 1,
                totalPrice = el.priceFrom(".priceStandard"),
                sizeMl = volume.sizeMlFrom()
              )
            ),
            thumbnailUrl = el.urlFrom(".js_itemImage")
          )
        }
      }
  }

  companion object {
    private val ROOT = root("https://www.innisandgunn.com/browse/c-All-Beers-14")
  }
}
