package watch.craft.scrapers

import org.jsoup.nodes.Document
import watch.craft.Format
import watch.craft.Format.KEG
import watch.craft.Offer
import watch.craft.Scraper
import watch.craft.Scraper.Job.Leaf
import watch.craft.Scraper.ScrapedItem
import watch.craft.dsl.*

// TODO - update bougieness validation
// TODO - mixed cases

class WildBeerScraper : Scraper {
  override val jobs = forPaginatedRoots(*ROOTS) { root: Document, keg ->
    root
      .selectMultipleFrom(".itemSmall")
      .map { el ->
        val title = el.textFrom(".itemSmallTitle")
        Leaf(title, el.urlFrom(".itemImageWrap a")) { doc ->
          val desc = doc.formattedTextFrom(".productDescription")
          val available = ".unavailableItemWrap" !in doc

          ScrapedItem(
            name = title.cleanse("\\d+L mini keg"),
            summary = if (keg) {
              desc.split("\n")[0].split("-")[0]
            } else {
              doc.textFrom("h4")
            },
            desc = desc,
            abv = desc.abvFrom(),
            available = available,
            offers = doc.extractOffers(
              desc,
              el.priceFrom(".priceStandard"),
              if (keg) KEG else desc.formatFrom(disallowed = listOf(KEG))
            ).toSet(),
            thumbnailUrl = el.urlFrom(".imageInnerWrap img")
          )
        }
      }
  }

  private fun Document.extractOffers(desc: String, indexPrice: Double, format: Format?): List<Offer> {
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
    private val ROOTS = arrayOf(
      root("https://www.wildbeerco.com/browse/c-Beers-17", false),
      root("https://www.wildbeerco.com/browse/c-Mini-Kegs-51", true)
    )
  }
}
