package watch.craft.enrichers

import watch.craft.Enricher
import watch.craft.Item
import kotlin.text.RegexOption.IGNORE_CASE

class Categoriser(categories: Map<String, List<String>>) : Enricher {
  private val keywords = categories
    .flatMap { (category, synonyms) -> synonyms.map { Keyword(it, it.toSweetRegex(), category) } }

  override fun enrich(item: Item): Item {
    val components = listOf(
      item.name,
      item.summary,
      item.desc
    )

    return item.copy(
      categories = keywords
        .filter { keyword -> components.any { it?.contains(keyword.regex) ?: false } }
        .pickMostSpecific()
        .map { it.category }
        .distinct()
    )
  }

  // Ensure we don't match against partial words
  private fun String.toSweetRegex() = "(^|\\W)${Regex.escape(this)}($|\\W)".toRegex(IGNORE_CASE)

  private fun List<Keyword>.pickMostSpecific(): List<Keyword> {
    val output = mutableListOf<Keyword>()

    sortedByDescending { it.raw.length }
      .forEach { candidate ->
        if (output.none { it.raw.contains(candidate.raw, ignoreCase = true) }) {
          output += candidate
        }
      }

    return output
  }

  private data class Keyword(
    val raw: String,
    val regex: Regex,
    val category: String
  )
}
