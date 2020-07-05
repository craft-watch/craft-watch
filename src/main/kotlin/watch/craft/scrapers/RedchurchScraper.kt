package watch.craft.scrapers

import org.jsoup.nodes.Document
import watch.craft.*
import watch.craft.Scraper.IndexEntry
import watch.craft.Scraper.ScrapedItem
import java.net.URI

class RedchurchScraper : Scraper {
  override val brewery = Brewery(
    shortName = "Redchurch",
    name = "Redchurch Brewery",
    location = "Harlow, Essex",
    websiteUrl = URI("https://redchurch.beer/")
  )
  override val rootUrls = listOf(URI("https://redchurch.store/"))

  override fun scrapeIndex(root: Document) = root
    .selectMultipleFrom(".product")
    .map { el ->
      val title = el.selectFrom(".product__title")
      val rawName = title.text()

      IndexEntry(rawName, title.hrefFrom("a")) { doc ->
        val nameParts = rawName.extract(regex = "(Mixed Case - )?(.*)")
        val mixed = !nameParts[1].isBlank()
        val sizeMl = doc.maybeExtractFrom(regex = "(\\d+)(ML|ml)")?.get(1)?.toInt()
        val abv = doc.maybeExtractFrom(regex = "(\\d+(\\.\\d+)?)%")?.get(1)?.toDouble()

        if (!mixed && sizeMl == null && abv == null) {
          throw SkipItemException("Can't identify ABV or size for non-mixed case, so assume it's not a beer")
        }

        val bestDeal = doc.extractBestDeal()

        ScrapedItem(
          thumbnailUrl = doc.srcFrom(".product-single__photo")
            .toString()
            .replace("\\?.*".toRegex(), "")
            .toUri(),
          name = nameParts[2],
          desc = doc.maybeWholeTextFrom(".product-single__description"),
          mixed = mixed,
          sizeMl = sizeMl,
          abv = abv,
          available = el.maybeSelectFrom(".sold-out-text") == null,
          numItems = bestDeal.numItems,
          price = bestDeal.price
        )
      }
    }

  private fun Document.extractBestDeal(): ItemPrice {
    @Suppress("UNCHECKED_CAST")
    val winner = (jsonFrom<Data>("#ProductJson-product-template").variants)
      .map {
        Variant(title = it.title, price = it.price / 100.0)
      }
      .maxBy { it.price }!!   // Assume highest price gives us the best deal

    return ItemPrice(
      numItems = winner.title.maybeExtract("^(\\d+)")?.get(1)?.toInt()
        ?: throw SkipItemException("Don't know how to identify number of items"),
      price = winner.price
    )
  }

  private data class Variant(
    val title: String,
    val price: Double
  )

  private data class ItemPrice(
    val numItems: Int,
    val price: Double
  )

  private data class Data(
    val variants: List<Variant>
  ) {
    data class Variant(
      val title: String,
      val price: Int
    )
  }
}
