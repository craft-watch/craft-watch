package choliver.neapi

import choliver.neapi.scrapers.*
import com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

fun main() {
  val getter = HttpGetter(CACHE_DIR)
  val executor = Executor(getter)
  val mapper = jacksonObjectMapper().enable(INDENT_OUTPUT)

  INVENTORY_JSON_FILE.outputStream().use { ostream ->
    mapper.writeValue(ostream, executor.scrapeAll(
      BoxcarScraper(),
      CanopyScraper(),
      FourpureScraper(),
      GipsyHillScraper(),
      HowlingHopsScraper(),
      PillarsScraper(),
      PressureDropScraper(),
      VillagesScraper()
    ))
  }
}
