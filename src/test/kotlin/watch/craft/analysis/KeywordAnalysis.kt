package watch.craft.analysis

import com.fasterxml.jackson.module.kotlin.readValue
import watch.craft.INVENTORY_JSON_FILE
import watch.craft.Inventory
import watch.craft.mapper


fun main() {
  val items = mapper().readValue<Inventory>(INVENTORY_JSON_FILE).items

  val reverseMap = SYNONYMS
    .flatMap { (category, synonyms) -> synonyms.map { it to category } }
    .associate { it }

  val categorised = items.associate { item ->
    val sources = listOf(
      item.name,
      item.summary,
      item.desc
    )

    val categories = reverseMap.keys
      .filter { keyword -> sources.any { it?.contains(keyword, ignoreCase = true) ?: false } }
      .pickMostSpecific()
      .map { reverseMap[it] }
      .distinct()

     item to categories
  }

  println("==== NO CATEGORY ====")
  categorised
    .filter { (_, v) -> v.isEmpty() }
    .forEach { (k, _) -> println(k.name) }

  println("==== MULTIPLE CATEGORIES ====")
  categorised
    .filter { (k, v) -> (v.size > 1) && (!k.mixed) }
    .forEach { (k, v) -> println("${k.name}: ${v}") }
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
)

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
