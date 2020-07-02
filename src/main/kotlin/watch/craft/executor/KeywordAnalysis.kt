package watch.craft.executor

import watch.craft.FatalScraperException
import watch.craft.Item

// TODO - needs to perform whole-word comparison ("IPA" may appear as part of a normal word)
fun Item.addCategories(): Item {
  val sources = listOf(
    name,
    summary,
    desc
  )

  val categories = SYNONYMS.keys
    .filter { keyword -> sources.any { it?.contains(keyword, ignoreCase = true) ?: false } }
    .pickMostSpecific()
    .map { SYNONYMS[it] ?: throw FatalScraperException("Unexpectedly weird") }
    .distinct()

  return copy(categories = categories)
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

private val SYNONYMS = mapOf(
  "IPA" to listOf(
    "IPA",
    "DIPA",
    "DDH",
    "New England IPA",
    "NEIPA",
    "India Pale Ale",
    "XPA",
    "Extra Pale Ale"
  ),
  "Pale" to listOf(
    "Pale",
    "Pale Ale"
  ),
  "Dark" to listOf(
    "Porter",
    "Stout",
    "Red Ale",
    "Dark"
  ),
  "Pils / Lager" to listOf(
    "Pils",
    "Lager",
    "India Pale Lager"
  ),
  "Sours / Gose" to listOf(
    "Sour",
    "Gose"
  ),
  "Bitter" to listOf(
    "Bitter",
    "Copper Ale",
    "Golden Ale"
  ),
  "Cider" to listOf(
    "Cider"
  )
).flatMap { (category, synonyms) -> synonyms.map { it to category } }
  .associate { it }
