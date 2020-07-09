package watch.craft.scrapers

import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import watch.craft.Brewery
import watch.craft.Offer
import watch.craft.Scraper
import watch.craft.Scraper.Job.Leaf
import watch.craft.Scraper.ScrapedItem
import watch.craft.SkipItemException
import watch.craft.utils.*
import java.net.URI

class FourpureScraper : Scraper {
  override val brewery = Brewery(
    shortName = "Fourpure",
    name = "Fourpure Brewing Co",
    location = "Bermondesy, London",
    websiteUrl = URI("https://www.fourpure.com/")
  )

  override val jobs = forRootUrls(ROOT_URL) { root ->
    root
      .selectMultipleFrom(".itemsBrowse li")
      .map { el ->
        val a = el.selectFrom("a")
        val rawName = el.textFrom(".content h3")

        Leaf(rawName, a.hrefFrom()) { doc ->
          if (el.title().contains("pack", ignoreCase = true)) {
            throw SkipItemException("Can't calculate price-per-can for packs")
          }

          val parts = extractVariableParts(doc)
          ScrapedItem(
            name = parts.name,
            desc = doc.maybe { formattedTextFrom(".productDetailsWrap .innerContent") },
            abv = doc.extractFrom(".brewSheet", "Alcohol By Volume: (\\d+(\\.\\d+)?)").doubleFrom(1),
            available = true,
            offers = setOf(
              Offer(
                totalPrice = el.selectFrom(".priceNow, .priceStandard").priceFrom(".GBP"),
                keg = parts.keg,
                sizeMl = parts.sizeMl
              )
            ),
            thumbnailUrl = a.srcFrom("img")
          )
        }
      }
  }

  private data class VariableParts(
    val name: String,
    val sizeMl: Int,
    val keg: Boolean = false
  )

  private fun extractVariableParts(itemDoc: Document): VariableParts {
    val title = itemDoc.textFrom(".itemTitle h1")
    return if (title.contains("minikeg", ignoreCase = true)) {
      VariableParts(
        name = title.extract("([^\\d]+) ")[1],
        sizeMl = title.sizeMlFrom(),
        keg = true
      )
    } else {
      VariableParts(
        name = title.extract("([^\\d]+)( \\d+ml)?")[1],  // Strip size in title
        sizeMl = itemDoc.sizeMlFrom(".quickBuy")
      )
    }
  }

  private fun Element.title() = textFrom("h3")

  companion object {
    private val ROOT_URL = URI("https://www.fourpure.com/browse/c-Our-Beers-5/")
  }
}
