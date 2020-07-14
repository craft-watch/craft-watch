package watch.craft.executor

import watch.craft.Item
import watch.craft.executor.Categoriser.Component.*
import kotlin.text.RegexOption.IGNORE_CASE

class Categoriser(categories: Map<String, List<Synonym>>) {
  data class Synonym(
    val pattern: String,
    val components: Set<Component> = values().toSet()
  )

  enum class Component {
    NAME,
    SUMMARY,
    DESC
  }

  private val candidates = categories
    .flatMap { (category, synonyms) -> synonyms.map { Candidate(it, it.toSweetRegex(), category) } }

  fun enrich(item: Item): Item {
    return item.copy(
      categories = candidates
        .filter { candidate ->
          item
            .componentsFor(candidate.synonym)
            .any { it?.contains(candidate.regex) ?: false }
        }
        .pickMostSpecific()
        .map { it.category }
        .distinct()
    )
  }

  private fun Item.componentsFor(synonym: Synonym) = synonym.components.map {
    when (it) {
      NAME -> name
      SUMMARY -> summary
      DESC -> desc
    }
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
