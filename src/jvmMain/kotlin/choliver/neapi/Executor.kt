package choliver.neapi

import choliver.neapi.model.Inventory
import choliver.neapi.model.Item
import choliver.neapi.scrapers.BoxcarScraper
import choliver.neapi.scrapers.GipsyHillScraper
import choliver.neapi.scrapers.HowlingHopsScraper
import choliver.neapi.scrapers.VillagesScraper
import com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.jsoup.Jsoup
import java.io.File
import java.net.URI

class Executor(
  private val getter: HttpGetter
) {
  fun scrapeAll() = Inventory(
    items = SCRAPERS.flatMap { scraper ->
      scraper.scrape(Jsoup.parse(getter.get(scraper.rootUrl)))
        .map {
          Item(
            brewery = scraper.name,
            name = it.name,
            abv = it.abv?.toFloat(),
            price = it.price.toFloat(),
            available = it.available,
            thumbnailUrl = it.thumbnailUrl?.let { url -> scraper.rootUrl.resolve(url).toString() },
            url = scraper.rootUrl.resolve(it.url).toString()
          )
        }
    }
  )

  companion object {
    private val SCRAPERS = listOf(
      BoxcarScraper(),
      GipsyHillScraper(),
      HowlingHopsScraper(),
      VillagesScraper()
    )

    private val CACHE_DIR = File("cache")

    @JvmStatic
    fun main(args: Array<String>) {
      val getter = HttpGetter(CACHE_DIR)
      val executor = Executor(getter)
      val mapper = jacksonObjectMapper().enable(INDENT_OUTPUT)

      File("src/jsMain/resources/inventory.json").outputStream().use { ostream ->
        mapper.writeValue(ostream, executor.scrapeAll())
      }
    }
  }
}
