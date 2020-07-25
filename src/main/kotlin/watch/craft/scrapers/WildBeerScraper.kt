package watch.craft.scrapers

import org.jsoup.nodes.Document
import watch.craft.Format.KEG
import watch.craft.Offer
import watch.craft.Scraper

import watch.craft.Scraper.Node.ScrapedItem
import watch.craft.dsl.*

class WildBeerScraper : Scraper {
  override val root = forPaginatedRoots(*ROOTS) { root: Document, keg ->
    root
      .selectMultipleFrom(".itemSmall")
      .map { el ->
        val title = el.textFrom(".itemSmallTitle")
        work(title, el.urlFrom(".itemImageWrap a")) { doc ->
          val desc = doc.formattedTextFrom(".productDescription")
          val available = ".unavailableItemWrap" !in doc
          val format = if (keg) KEG else desc.formatFrom(disallowed = listOf(KEG))

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
            offers = if (available) {
              doc.selectMultipleFrom(".itemDescription .sizeLabel")
                .map { label ->
                  val sizeName = label.selectFrom(".sizeName")
                  val priceNow = label.selectFrom(".priceNow")
                  Offer(
                    quantity = sizeName.maybe { quantityFrom() } ?: 1,
                    totalPrice = priceNow.priceFrom(),
                    sizeMl = sizeName.sizeMlFrom(),
                    format = format
                  )
                }.toSet()
            } else {
              setOf(
                Offer(
                  quantity = 1,
                  totalPrice = el.priceFrom(".priceStandard"),
                  sizeMl = maybeAnyOf(
                    { desc.sizeMlFrom() },
                    { title.sizeMlFrom() }
                  ),
                  format = format
                )
              )
            },
            thumbnailUrl = el.urlFrom(".imageInnerWrap img")
          )
        }
      }
  }

  companion object {
    private val ROOTS = arrayOf(
      root("https://www.wildbeerco.com/browse/c-Beers-17", false),
      root("https://www.wildbeerco.com/browse/c-Mini-Kegs-51", true)
    )
  }
}
