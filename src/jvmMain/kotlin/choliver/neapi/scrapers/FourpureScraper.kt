package choliver.neapi.scrapers

import choliver.neapi.ParsedItem
import choliver.neapi.Scraper
import choliver.neapi.Scraper.Context
import org.jsoup.nodes.Element
import java.net.URI

class FourpureScraper : Scraper {
  override val name = "Fourpure"

  override fun Context.scrape() = request(ROOT_URL) { doc -> doc
    .select(".itemsBrowse li")
    .filterNot { el -> el.getName().contains("minikeg", ignoreCase = true) }  // Kegs break our can-based model, so ignore
    .filterNot { el -> el.getName().contains("pack", ignoreCase = true) }  // Can't figure out how to extract price-per-can from packs, so ignore
    .map { el ->
      val a = el.selectFirst("a")
      val url = URI(a.attr("href").trim())
      val subdoc = request(url) { it }

      ParsedItem(
        thumbnailUrl = ROOT_URL.resolve(a.selectFirst("img").attr("src").trim()),
        url = url,
        name = "([^\\d]+)( \\d+ml)?".toRegex().find(el.getName())!!.groupValues[1],  // Strip size embedded in name
        abv = "Alcohol By Volume: (\\d+\\.\\d+)".toRegex()
          .find(subdoc.selectFirst(".brewSheet").text())!!
          .groupValues[1]
          .toBigDecimal(),
        summary = null,
        sizeMl = "(\\d+)ml".toRegex()
          .find(subdoc.selectFirst(".quickBuy").text())!!
          .groupValues[1]
          .toInt(),
        available = true,
        pricePerCan = (el.selectFirst(".priceNow") ?: el.selectFirst(".priceStandard"))
          .selectFirst(".GBP").text().removePrefix("£").toBigDecimal()
      )
    }
  }

  private fun Element.getName() = selectFirst("h3").text().trim()

  companion object {
    private val ROOT_URL = URI("https://www.fourpure.com/browse/c-Our-Beers-5/")
  }
}
