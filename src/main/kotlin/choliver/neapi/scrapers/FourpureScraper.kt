package choliver.neapi.scrapers

import choliver.neapi.*
import choliver.neapi.Scraper.IndexEntry
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.net.URI

class FourpureScraper : Scraper {
  override val name = "Fourpure"
  override val rootUrl = URI("https://www.fourpure.com/browse/c-Our-Beers-5/")

  override fun scrapeIndex(root: Document) = root
    .selectMultipleFrom(".itemsBrowse li")
    .filterNot { el -> el.title().contains("pack", ignoreCase = true) }  // Can't figure out how to extract price-per-can from packs, so ignore
    .map { el ->
      val a = el.selectFrom("a")

      IndexEntry(a.hrefFrom()) { doc ->
        val parts = extractVariableParts(doc)

        ScrapedItem(
          thumbnailUrl = a.srcFrom("img"),
          name = parts.name,
          abv = doc.extractFrom(".brewSheet", "Alcohol By Volume: (\\d+\\.\\d+)")!![1].toDouble(),
          summary = parts.summary,
          sizeMl = parts.sizeMl,
          available = true,
          perItemPrice = el.selectFrom(".priceNow, .priceStandard").priceFrom(".GBP")
        )
      }
    }

  private data class VariableParts(
    val name: String,
    val sizeMl: Int,
    val summary: String? = null
  )

  private fun extractVariableParts(itemDoc: Document): VariableParts {
    val title = itemDoc.textFrom(".itemTitle h1")
    return if (title.contains("minikeg", ignoreCase = true)) {
      val parts = title.extract("([^\\d]+) (\\d+)L.*")!!
      VariableParts(
        name = parts[1],
        sizeMl = parts[2].toInt() * 1000,
        summary = "Minikeg"
      )
    } else {
      VariableParts(
        name = title.extract("([^\\d]+)( \\d+ml)?")!![1],  // Strip size in title
        sizeMl = itemDoc.extractFrom(".quickBuy", "(\\d+)ml")!![1].toInt()
      )
    }
  }


  private fun Element.title() = textFrom("h3")
}
