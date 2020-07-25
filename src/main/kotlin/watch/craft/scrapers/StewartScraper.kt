package watch.craft.scrapers

import watch.craft.Offer
import watch.craft.Scraper

import watch.craft.Scraper.Node.ScrapedItem
import watch.craft.dsl.*

class StewartScraper : Scraper {
  override val root = forRoots(ROOT) { root ->
    root
      .selectMultipleFrom("#browse li .itemWrap")
      .map { el ->
        val a = el.selectFrom("h2 a")

        work(a.text(), a.urlFrom()) { doc ->

          ScrapedItem(
            thumbnailUrl = el.urlFrom(".imageInnerWrap img"),
            name = removeSizeSuffix(a.text()),
            summary = el.maybe { textFrom(".itemStyle") },
            abv = doc.orSkip("Couldn't find ABV") { abvFrom(".alco") },
            available = true,
            offers = setOf(
              Offer(
                totalPrice = doc.priceFrom(".priceNow"),
                sizeMl = doc.orSkip("Couldn't find size") { sizeMlFrom(".volume") }
              )
            )
          )
        }
      }
  }

  private fun removeSizeSuffix(str: String) = if (str.endsWith("ml")) {
    str.extract(regex = "^(.+?)( \\d+ml)")[1]
  } else {
    str
  }

  companion object {
    private val ROOT = root("https://www.stewartbrewing.co.uk/browse/c-Build-Your-Own-12-Pack-37?view_all=true")
  }
}
