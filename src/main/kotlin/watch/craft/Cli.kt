package watch.craft

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.choice
import watch.craft.executor.Executor

class Cli : CliktCommand(name = "scraper") {
  private val scraperDetails = SCRAPERS.associateBy { it.brewery.id }

  private val listScrapers by option("--list-scrapers", "-l").flag()
  private val forceDownload by option("--force-download", "-f").flag()
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
    val structure = StorageStructure(dateString, forceDownload)
    val results = ResultsManager(structure)
    val executor = Executor(results = results, createRetriever = structure.createRetriever)
    val inventory = executor.scrape(scrapers.ifEmpty { SCRAPERS })
    results.write(inventory)
  }
}

fun main(args: Array<String>) = Cli().main(args)

