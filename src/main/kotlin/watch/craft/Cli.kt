package watch.craft

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.choice
import watch.craft.executor.Executor

class Cli : CliktCommand(name = "scraper") {
  private val scraperDetails = SCRAPERS.associateBy { it.brewery.shortName.toSafeName() }

  private val listScrapers by option("--list-scrapers", "-l").flag()
  private val dateString by option("--date", "-d")
  private val scrapers by argument().choice(scraperDetails).multiple()

  override fun run() {
    when {
      listScrapers -> executeListScrapers()
      else -> executeScrape()
    }
  }

  private fun executeListScrapers() {
    scraperDetails.keys.sorted().forEach(::println)
  }

  private fun executeScrape() {
    val setup = Setup(dateString)
    val results = ResultsManager(setup)
    val executor = Executor(results = results, createRetriever = setup.createRetriever)
    val inventory = executor.scrape(scrapers.ifEmpty { SCRAPERS })
    results.write(inventory)
  }

  private fun String.toSafeName() = toLowerCase().replace("[^0-9a-z]".toRegex(), "-")
}

fun main(args: Array<String>) = Cli().main(args)

