package choliver.neapi.scrapers

import choliver.neapi.*
import choliver.neapi.Scraper.IndexEntry
import choliver.neapi.Scraper.Result.Item
import choliver.neapi.Scraper.Result.Skipped
import org.jsoup.nodes.Document
import java.net.URI

class StewartScraper : Scraper {
  override val name = "Stewart Brewing"
  override val rootUrl = URI("https://www.stewartbrewing.co.uk/browse/c-Build-Your-Own-12-Pack-37?view_all=true")

  override fun scrapeIndex(root: Document) = root
    .selectMultipleFrom("#browse li .itemWrap")
    .map { el ->
      val a = el.selectFrom("h2 a")

      IndexEntry(a.hrefFrom()) { doc ->
        val alco = doc.maybeSelectFrom(".alco")
        val volume = doc.maybeSelectFrom(".volume")

        if (alco == null || volume == null) {
          Skipped("Couldn't find ABV or volume")
        } else {
          Item(
            thumbnailUrl = el.srcFrom(".imageInnerWrap img"),
            name = removeSizeSuffix(a.text()),
            summary = el.maybeTextFrom(".itemStyle"),
            abv = alco.extractFrom(regex = "(\\d+(\\.\\d+)?)%")!![1].toDouble(),
            sizeMl = volume.extractFrom(regex = "(\\d+)ml")!![1].toInt(),
            available = true,
            perItemPrice = doc.extractFrom(".priceNow", "£(\\d+\\.\\d+)")!![1].toDouble()
          )
        }
      }
    }

  private fun removeSizeSuffix(str: String) = if (str.endsWith("ml")) {
    str.extract(regex = "^(.+?)( \\d+ml)")!![1]
  } else {
    str
  }
}
