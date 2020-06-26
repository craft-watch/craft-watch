package choliver.neapi

import choliver.neapi.getters.HttpGetter
import choliver.neapi.getters.cached
import choliver.neapi.scrapers.*
import com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option

class Cli : CliktCommand(name = "scraper") {
  private val withoutCache by option("--without-cache", "-w").flag()

  private val mapper = jacksonObjectMapper().enable(INDENT_OUTPUT)
  private val scrapers = listOf(
    BoxcarScraper(),
    CanopyScraper(),
    FivePointsScraper(),
    FourpureScraper(),
    GipsyHillScraper(),
    HowlingHopsScraper(),
    PillarsScraper(),
    PressureDropScraper(),
    StewartScraper(),
    VillagesScraper()
  )

  override fun run() {
    val getter = HttpGetter()
      .let { if (withoutCache) it else it.cached(CACHE_DIR) }

    val executor = Executor(getter)

    INVENTORY_JSON_FILE.outputStream().use { ostream ->
      mapper.writeValue(ostream, executor.scrapeAll(*scrapers.toTypedArray()))
    }
  }
}

fun main(args: Array<String>) = Cli().main(args)
