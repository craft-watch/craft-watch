package watch.craft.scrapers

import org.jsoup.nodes.Document
import watch.craft.*
import watch.craft.Scraper.IndexEntry
import watch.craft.Scraper.Item
import java.net.URI
import kotlin.text.RegexOption.IGNORE_CASE

class ThornbridgeScraper : Scraper {
  override val name = "Thornbridge"
  // TODO - URI("https://shop.thornbridgebrewery.co.uk/collections/smart-collection?view=list")
  override val rootUrls = listOf(
    URI("https://shop.thornbridgebrewery.co.uk/collections/pick-and-mix-beers?view=list")
  )

  override fun scrapeIndex(root: Document) = root
    .selectMultipleFrom(".grid-uniform > .grid-item")
    .map { el ->
      val rawName = el.textFrom(".h6")

      IndexEntry(rawName, el.hrefFrom("a")) { doc ->
        val desc = doc.selectFrom(".product-description")

        val allStats = desc.selectMultipleFrom("p")
          .mapNotNull { it.maybeExtractFrom(regex = "((\\d+) x .*)?ABV (\\d+(\\.\\d+)?)%( / (\\d+)ml)?") }

        if (allStats.isEmpty()) {
          throw SkipItemException("Can't find any stats")
        }

        val numCans = allStats.sumBy { it[2].toIntOrNull() ?: 1 }
        println(numCans)



        val parts = rawName.extract("(.*?)\\W+(\\d(\\.\\d+)?)%\\W+(.*)")
        val descText = desc.text()

//        val numCans = descText.maybeExtract(regex = "(\\d+)\\s*x")?.get(1)?.toInt()
//          ?: rawName.maybeExtract(regex = "(\\d+)\\s*x")?.get(1)?.toInt()
//          ?: 1

        val sizeMl = desc.maybeExtractFrom(regex = "(\\d+)ml")?.get(1)?.toInt()

        Item(
          thumbnailUrl = doc.srcFrom(".product__image-wrapper img"),
          name = parts[1].replace(" (bottle|can)$".toRegex(IGNORE_CASE), ""),
          summary = parts[4],
          desc = desc.selectMultipleFrom("p").joinToString("\n") { it.text() },
          mixed = allStats.size > 1,
          sizeMl = sizeMl,
          abv = parts[2].toDouble(),
          available = "sold-out" !in el.classNames(),
          perItemPrice = el.priceFrom(".product-item--price").divideAsPrice(numCans)
        )
      }
    }
}
