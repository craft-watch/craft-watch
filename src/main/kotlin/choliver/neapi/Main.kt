package choliver.neapi

import choliver.neapi.scrapers.BoxcarScraper
import choliver.neapi.scrapers.GipsyHillScraper
import choliver.neapi.scrapers.HowlingHopsScraper
import choliver.neapi.scrapers.VillagesScraper
import com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.jsoup.Jsoup
import java.net.URI

class Main(
  private val getUrl: (URI) -> String
) {
  fun scrapeAll() = SCRAPERS.flatMap { scraper ->
    scraper.scrape(Jsoup.parse(getUrl(scraper.rootUrl)))
      .map {
        Item(
          brewery = scraper.name,
          name = it.name,
          price = it.price,
          available = it.available,
          url = scraper.rootUrl.resolve(it.url)
        )
      }
  }

  companion object {
    private val SCRAPERS = listOf(
      BoxcarScraper(),
      GipsyHillScraper(),
      HowlingHopsScraper(),
      VillagesScraper()
    )

    @JvmStatic
    fun main(args: Array<String>) {
      val samples = SCRAPERS.associate {
        it.rootUrl to {}.javaClass.getResource("/samples/${it.name.toLowerCase().replace(" ", "-")}.html").readText()
      }

      val main = Main(getUrl = { samples[it] ?: error("Unknown URL ${it}") })

      val mapper = jacksonObjectMapper().enable(INDENT_OUTPUT)

      println(mapper.writeValueAsString(main.scrapeAll()))
    }
  }
}
