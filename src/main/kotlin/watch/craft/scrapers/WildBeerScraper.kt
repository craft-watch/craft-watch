package watch.craft.scrapers

import org.jsoup.nodes.Document
import watch.craft.Format
import watch.craft.Format.KEG
import watch.craft.Offer
import watch.craft.Scraper
import watch.craft.Scraper.Job.Leaf
import watch.craft.Scraper.ScrapedItem
import watch.craft.dsl.*
import java.net.URI

class WildBeerScraper : Scraper {
  override val jobs = forPaginatedRootUrl(ROOT_URL) { root ->
    root
      .selectMultipleFrom(".itemSmall")
      .map { el ->
        val title = el.textFrom(".itemSmallTitle")
        Leaf(title, el.urlFrom(".itemImageWrap a")) { doc ->
          val desc = doc.formattedTextFrom(".productDescription")
          val available = ".unavailableItemWrap" !in doc

          ScrapedItem(
            name = title,
            summary = doc.textFrom("h4"),
            desc = desc,
            abv = el.abvFrom(".itemAlco"),
            available = available,
            offers = doc.extractOffers(
              desc,
              el.priceFrom(".priceStandard")
            ).toSet(),
            thumbnailUrl = el.urlFrom(".imageInnerWrap img")
          )
        }
      }
  }

  private fun Document.extractOffers(desc: String, indexPrice: Double): List<Offer> {
    val format = desc.formatFrom(disallowed = listOf(KEG)) // TODO - gross

    return maybe { selectMultipleFrom(".itemDescription .sizeLabel") }
      ?.map { label ->
        val sizeName = label.selectFrom(".sizeName")
        val priceNow = label.selectFrom(".priceNow")
        Offer(
          quantity = sizeName.maybe { quantityFrom() } ?: 1,
          totalPrice = priceNow.priceFrom(),
          sizeMl = sizeName.sizeMlFrom(),
          format = format
        )
      }
      ?: listOf(
        Offer(
          quantity = 1,
          totalPrice = indexPrice,
          sizeMl = desc.sizeMlFrom(),
          format = format
        )
      )
  }

  companion object {
    // TODO - keg pages, etc.
    private val ROOT_URL = URI("https://www.wildbeerco.com/browse/c-Beers-17")
  }
}
