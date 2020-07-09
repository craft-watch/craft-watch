package watch.craft.scrapers

import watch.craft.Brewery
import watch.craft.Scraper
import watch.craft.Scraper.Job.Leaf
import watch.craft.Scraper.ScrapedItem
import watch.craft.utils.*
import java.net.URI

class StewartScraper : Scraper {
  override val brewery = Brewery(
    shortName = "Stewart Brewing",
    name = "Stewart Brewing",
    location = "Loanhead, Midlothian",
    websiteUrl = URI("https://www.stewartbrewing.co.uk/")
  )

  override val jobs = forRootUrls(ROOT_URL) { root ->
    root
      .selectMultipleFrom("#browse li .itemWrap")
      .map { el ->
        val a = el.selectFrom("h2 a")

        Leaf(a.text(), a.hrefFrom()) { doc ->

          ScrapedItem(
            thumbnailUrl = el.srcFrom(".imageInnerWrap img"),
            name = removeSizeSuffix(a.text()),
            summary = el.maybe { textFrom(".itemStyle") },
            abv = doc.orSkip("Couldn't find ABV") { abvFrom(".alco") },
            sizeMl = doc.orSkip("Couldn't find size") { sizeMlFrom(".volume") },
            available = true,
            totalPrice = doc.priceFrom(".priceNow")
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
    private val ROOT_URL = URI("https://www.stewartbrewing.co.uk/browse/c-Build-Your-Own-12-Pack-37?view_all=true")
  }
}
