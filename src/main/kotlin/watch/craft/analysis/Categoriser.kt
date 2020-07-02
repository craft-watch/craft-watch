package watch.craft.analysis

import watch.craft.FatalScraperException
import watch.craft.Item

class Categoriser(categories: Map<String, List<String>>) {
  private val keywords = categories
    .flatMap { (category, synonyms) -> synonyms.map { it to category } }
    .associate { it }

  // TODO - needs to perform whole-word comparison ("IPA" may appear as part of a normal word)
  fun categorise(item: Item): List<String> {
    val sources = listOf(
      item.name,
      item.summary,
      item.desc
    )

    return keywords.keys
      .filter { keyword -> sources.any { it?.contains(keyword, ignoreCase = true) ?: false } }
      .pickMostSpecific()
      .map { keywords[it] ?: throw FatalScraperException("Unexpectedly weird") }
      .distinct()
  }

  private fun List<String>.pickMostSpecific(): List<String> {
    val output = mutableListOf<String>()

    sortedByDescending { it.length }
      .forEach { candidate ->
        if (output.none { it.contains(candidate, ignoreCase = true) }) {
          output += candidate
        }
      }

    return output
  }
}
