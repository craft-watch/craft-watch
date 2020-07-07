package watch.craft.scrapers

import org.jsoup.nodes.Document
import watch.craft.*
import watch.craft.Scraper.Job.Leaf
import watch.craft.Scraper.ScrapedItem
import java.net.URI
import kotlin.text.RegexOption.IGNORE_CASE

class MarbleScraper : Scraper {
  override val brewery = Brewery(
    shortName = "Marble",
    name = "Marble Beers",
    location = "Salford, Greater Manchester",
    websiteUrl = URI("https://marblebeers.com/")
  )

  override val jobs = forRootUrls(ROOT_URL) { root ->
    root
      .selectMultipleFrom(".product")
      .map { el ->
        val name = el.textFrom(".woocommerce-loop-product__title")

        Leaf(name, el.hrefFrom(".woocommerce-LoopProduct-link")) { doc ->
          val attributes = doc.extractAttributes()
          val volumeDetails = attributes.extractVolumeDetails()

          val style = attributes.maybeGrab("Style")
          val mixed = style?.contains("mixed", ignoreCase = true) ?: false

          ScrapedItem(
            thumbnailUrl = el.srcFrom(".wp-post-image"),
            name = name
              .replace("\\s+\\d+l mini (keg|cask)$".toRegex(IGNORE_CASE), "")
              .replace("\\s+Case\\s+\\(\\d+ Cans\\)$".toRegex(IGNORE_CASE), "")
              .replace("\\d+$".toRegex(), "")
              .toTitleCase(),
            summary = if (mixed) null else style,
            desc = doc.maybe { wholeTextFrom(".woocommerce-product-details__short-description") },
            mixed = mixed,
            keg = attributes.maybeGrab("Packaging")?.contains("keg", ignoreCase = true) ?: false,
            sizeMl = volumeDetails.sizeMl,
            abv = attributes.grab("ABV").maybeExtract("(\\d+(\\.\\d+)?)")?.get(1)?.toDouble(),
            available = ".out-of-stock" !in doc,
            numItems = volumeDetails.numItems,
            price = doc.priceFrom(".price")
          )
        }
      }
  }

  private data class VolumeDetails(
    val sizeMl: Int,
    val numItems: Int
  )

  private fun Map<String, String>.extractVolumeDetails(): VolumeDetails {
    val rawVolume = maybeGrab("Volume") ?: grab("Packaging")
    return VolumeDetails(
      sizeMl = rawVolume.sizeMlFrom(),
      numItems = rawVolume.maybe { extract("(\\d+) x") }?.let { it[1].toInt() }
        ?: maybeGrab("Unit Size")?.toIntOrNull()
        ?: 1
    )
  }

  private fun Document.extractAttributes() = maybeSelectMultipleFrom(".shop_attributes tr")
    .ifEmpty { throw SkipItemException("No attributes, so can't process") }
    .associate { it.textFrom("th") to it.textFrom("td") }

  companion object {
    private val ROOT_URL = URI("https://marblebeers.com/product-category/?term=beers")
  }
}
