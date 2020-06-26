package choliver.neapi

import choliver.neapi.getters.HttpGetter
import choliver.neapi.getters.NewCachingGetter
import choliver.neapi.getters.cached
import choliver.neapi.scrapers.*
import choliver.neapi.storage.StorageThinger
import com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import java.time.Instant

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
    val storage = StorageThinger(STORAGE_DIR, Instant.now())
    val getter = NewCachingGetter(storage, HttpGetter())

    val executor = Executor(getter)

    val inventory = executor.scrapeAll(*scrapers.toTypedArray())
    storage.writeResults(
      "inventory.json",
      mapper.writeValueAsBytes(inventory)
    )
  }
}

fun main(args: Array<String>) = Cli().main(args)
