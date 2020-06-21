package choliver.neapi.scrapers

import choliver.neapi.ParsedItem
import choliver.neapi.Scraper
import choliver.neapi.Scraper.Context
import org.jsoup.nodes.Element
import java.net.URI

class FourpureScraper : Scraper {
  override val name = "Fourpure"

  override fun Context.scrape() = request(ROOT_URL)
    .select(".itemsBrowse li")
    .filterNot { el -> el.getName().contains("minikeg", ignoreCase = true) }  // Kegs break our can-based model, so ignore
    .filterNot { el -> el.getName().contains("pack", ignoreCase = true) }  // Can't figure out how to extract price-per-can from packs, so ignore
    .map { el ->
      val a = el.selectFirst("a")
      val url = URI(a.attr("href").trim())
      val itemDoc = request(url)

      ParsedItem(
        thumbnailUrl = ROOT_URL.resolve(a.selectFirst("img").attr("src").trim()),
        url = url,
        name = el.getName().extract("([^\\d]+)( \\d+ml)?")!![1],  // Strip size embedded in name
        abv = itemDoc.selectFirst(".brewSheet").text()
          .extract("Alcohol By Volume: (\\d+\\.\\d+)")!![1].toBigDecimal(),
        summary = null,
        sizeMl = itemDoc.selectFirst(".quickBuy").text()
          .extract("(\\d+)ml")!![1].toInt(),
        available = true,
        pricePerCan = (el.selectFirst(".priceNow") ?: el.selectFirst(".priceStandard"))
          .selectFirst(".GBP").text().removePrefix("£").toBigDecimal()
      )
    }

  private fun Element.getName() = selectFirst("h3").text().trim()

  companion object {
    private val ROOT_URL = URI("https://www.fourpure.com/browse/c-Our-Beers-5/")
  }
}
