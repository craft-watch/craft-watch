package watch.craft.enrichers

import watch.craft.Enricher
import watch.craft.Item
import kotlin.text.RegexOption.IGNORE_CASE

class Categoriser(categories: Map<String, List<Synonym>>) : Enricher {
  data class Synonym(
    val pattern: String
  )

  private val candidates = categories
    .flatMap { (category, synonyms) -> synonyms.map { Candidate(it, it.toSweetRegex(), category) } }

  override fun enrich(item: Item): Item {
    val components = listOf(
      item.name,
      item.summary,
      item.desc
    )

    return item.copy(
      categories = candidates
        .filter { candidate -> components.any { it?.contains(candidate.regex) ?: false } }
        .pickMostSpecific()
        .map { it.category }
        .distinct()
    )
  }

  // Ensure we don't match against partial words
  private fun Synonym.toSweetRegex() = "(^|\\W)${Regex.escape(pattern)}($|\\W)".toRegex(IGNORE_CASE)

  private fun List<Candidate>.pickMostSpecific(): List<Candidate> {
    val output = mutableListOf<Candidate>()

    sortedByDescending { it.synonym.pattern.length }
      .forEach { candidate ->
        if (output.none { it.synonym.pattern.contains(candidate.synonym.pattern, ignoreCase = true) }) {
          output += candidate
        }
      }

    return output
  }

  private data class Candidate(
    val synonym: Synonym,
    val regex: Regex,
    val category: String
  )
}
