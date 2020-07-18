package watch.craft.scrapers

import org.jsoup.nodes.Document
import watch.craft.Format.KEG
import watch.craft.Offer
import watch.craft.Scraper
import watch.craft.Scraper.Job.Leaf
import watch.craft.Scraper.ScrapedItem
import watch.craft.utils.*
import java.net.URI

class MarbleScraper : Scraper {
  override val jobs = forRootUrls(ROOT_URL) { root ->
    root
      .selectMultipleFrom(".product")
      .map { el ->
        val name = el.textFrom(".woocommerce-loop-product__title")

        Leaf(name, el.urlFrom(".woocommerce-LoopProduct-link")) { doc ->
          val attributes = doc.extractAttributes()
          val volumeDetails = attributes.extractVolumeDetails()

          val style = attributes.maybe { grab("Style") }
          val mixed = style?.containsMatch("mixed") ?: false
          val keg = attributes.maybe { grab("Packaging") }?.containsMatch("keg") ?: false

          ScrapedItem(
            thumbnailUrl = el.urlFrom(".wp-post-image"),
            name = name.cleanse(
              "\\s+\\d+l mini (keg|cask)$",
              "\\s+Case\\s+\\(\\d+ Cans\\)$",
              "\\d+$"
            ).toTitleCase(),
            summary = if (mixed) null else style,
            desc = doc.maybe { formattedTextFrom(".woocommerce-product-details__short-description") }?.ifBlank { null },
            mixed = mixed,
            abv = attributes.grab("ABV").maybe { abvFrom(noPercent = true) },
            available = ".out-of-stock" !in doc,
            offers = setOf(
              Offer(
                quantity = volumeDetails.numItems,
                totalPrice = doc.priceFrom(".price"),
                format = if (keg) KEG else null,
                sizeMl = volumeDetails.sizeMl
              )
            )
          )
        }
      }
  }

  private data class VolumeDetails(
    val sizeMl: Int,
    val numItems: Int
  )

  private fun Map<String, String>.extractVolumeDetails(): VolumeDetails {
    val rawVolume = maybe { grab("Volume") } ?: grab("Packaging")
    return VolumeDetails(
      sizeMl = rawVolume.sizeMlFrom(),
      numItems = rawVolume.maybe { quantityFrom() }
        ?: maybe { grab("Unit Size") }?.toIntOrNull()
        ?: 1
    )
  }

  private fun Document.extractAttributes() = orSkip("No attributes, so can't process") {
    selectMultipleFrom(".shop_attributes tr")
  }.associate { it.textFrom("th") to it.textFrom("td") }

  companion object {
    private val ROOT_URL = URI("https://marblebeers.com/product-category/?term=beers")
  }
}
