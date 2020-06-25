package choliver.neapi.scrapers

import choliver.neapi.*
import choliver.neapi.Scraper.Context
import java.net.URI

class PillarsScraper : Scraper {
  override val name = "Pillars"

  override fun Context.scrape() = //      val itemText = request(details.url).text()
//      val sizeMl = itemText.extract("(\\d+)ml")?.get(1)?.toInt()
    request(ROOT_URL)
      .shopifyItems()
      .mapNotNull { details ->
        val titleParts = extractTitleParts(details.title)

        val itemDoc = request(details.url)
        val descParts = itemDoc.extractFrom(
          ".product-single__description",
          "STYLE:\\s+(.+?)\\s+ABV:\\s+(\\d\\.\\d+)%"
        )

        // If we don't see these fields, assume we're not looking at a beer product
        if (descParts == null) {
          null
        } else {
          ScrapedItem(
            thumbnailUrl = details.thumbnailUrl,
            url = details.url,
            name = titleParts.name,
            summary = if (titleParts.keg) "Minikeg" else descParts[1].toTitleCase(),
            sizeMl = titleParts.sizeMl,
            abv = descParts[2].toDouble(),
            available = details.available,
            perItemPrice = details.price.divideAsPrice(titleParts.numItems)
          )
        }
      }
      .bestPricedItems()

  private data class TitleParts(
    val name: String,
    val sizeMl: Int? = null,
    val numItems: Int = 1,
    val keg: Boolean = false
  )

  private fun extractTitleParts(title: String) = when {
    title.contains("Case") -> {
      val parts = title.extract("(.*?) Case of (\\d+)")!!
      TitleParts(name = parts[1], numItems = parts[2].toInt())
    }
    title.contains("Keg") -> {
      val parts = title.extract("(.*?) (\\d+)L")!!
      TitleParts(name = parts[1], sizeMl = parts[2].toInt() * 1000, keg = true)
    }
    else -> TitleParts(name = title)
  }

  companion object {
    private val ROOT_URL = URI("https://shop.pillarsbrewery.com/collections/pillars-beers")
  }
}
