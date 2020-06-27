package choliver.neapi.scrapers

import choliver.neapi.*
import choliver.neapi.Scraper.IndexEntry
import choliver.neapi.Scraper.Item
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.jsoup.nodes.Document
import java.net.URI

class RedchurchScraper : Scraper {
  override val name = "Redchurch"
  override val rootUrl = URI("https://redchurch.store/")

  private val objectMapper = jacksonObjectMapper()

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

        Item(
          thumbnailUrl = doc.srcFrom(".product-single__photo")
            .toString()
            .replace("\\?.*".toRegex(), "")
            .toUri(),
          name = nameParts[2],
          summary = if (mixed) "Mixed case" else null,
          sizeMl = sizeMl,
          abv = abv,
          available = el.maybeSelectFrom(".sold-out-text") == null,
          perItemPrice = doc.extractBestPerItemPrice()
        )
      }
    }

  private fun Document.extractBestPerItemPrice(): Double {
    val json = selectFrom("#ProductJson-product-template")

    val winner = (objectMapper.readValue<Map<String, Any>>(json.data())["variants"] as List<Map<String, Any>>)
      .map {
        Variant(title = it["title"] as String, price = (it["price"] as Int) / 100.0)
      }
      .maxBy { it.price }!!   // Assume highest price gives us the best deal

    val numItems = winner.title.maybeExtract("^(\\d+)")?.get(1)?.toInt()
      ?: throw SkipItemException("Don't know how to identify number of items")

    return winner.price.divideAsPrice(numItems)
  }

  private data class Variant(
    val title: String,
    val price: Double
  )
}
