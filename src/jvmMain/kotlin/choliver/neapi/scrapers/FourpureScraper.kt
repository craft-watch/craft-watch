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
      val url = URI(a.hrefFrom())
      val itemDoc = request(url)

      ParsedItem(
        thumbnailUrl = ROOT_URL.resolve(a.srcFrom("img")),
        url = url,
        name = el.getName().extract("([^\\d]+)( \\d+ml)?")!![1],  // Strip size embedded in name
        abv = itemDoc.extractFrom(".brewSheet", "Alcohol By Volume: (\\d+\\.\\d+)")!![1].toDouble(),
        summary = null,
        sizeMl = itemDoc.extractFrom(".quickBuy", "(\\d+)ml")!![1].toInt(),
        available = true,
        pricePerCan = (el.selectFirst(".priceNow") ?: el.selectFirst(".priceStandard")).priceFrom(".GBP")
      )
    }

  private fun Element.getName() = textFrom("h3")

  companion object {
    private val ROOT_URL = URI("https://www.fourpure.com/browse/c-Our-Beers-5/")
  }
}
