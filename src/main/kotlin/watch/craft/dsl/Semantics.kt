package watch.craft.dsl

import org.jsoup.nodes.Element
import watch.craft.Format
import watch.craft.Format.*
import watch.craft.MalformedInputException
import kotlin.text.RegexOption.IGNORE_CASE

fun Element.priceFrom(cssQuery: String = ":root") = textFrom(cssQuery).priceFrom()
fun String.priceFrom() = maybe { extract("\\d+(\\.\\d+)?").doubleFrom(0) }
  ?: throw MalformedInputException("Can't extract price")

fun Element.abvFrom(
  cssQuery: String = ":root",
  prefix: String = "",
  noPercent: Boolean = false
) = textFrom(cssQuery).abvFrom(prefix, noPercent)

fun String.abvFrom(
  prefix: String = "",
  noPercent: Boolean = false
) = maybe { extract(prefix + DOUBLE_REGEX + (if (noPercent) "" else "\\s*%")).doubleFrom(1) }
  ?: throw MalformedInputException("Can't extract ABV")

fun Element.quantityFrom(cssQuery: String = ":root") = textFrom(cssQuery).quantityFrom()
fun String.quantityFrom(vararg moreRegexes: String) = maybeAnyOf(
  *(listOf("$INT_REGEX\\s*(?:x|×|-?pack)", "(?:x|×)\\s*$INT_REGEX") + moreRegexes.toList())
    .map { regex -> { str: String -> str.extract(regex).intFrom(1) } }
    .toTypedArray()
) ?: throw MalformedInputException("Can't extract quantity")

fun Element.sizeMlFrom(cssQuery: String = ":root") = textFrom(cssQuery).sizeMlFrom()
fun String.sizeMlFrom() = maybeAnyOf(
  { extract("$INT_REGEX\\s*ml(?:\\W|$)").intFrom(1) },
  { (extract("$DOUBLE_REGEX(?:\\s*|-)(?:litre|liter)(?:s?)(?:\\W|$)").doubleFrom(1) * 1000).toInt() },
  { (extract("$DOUBLE_REGEX\\s*l(?:\\W|$)").doubleFrom(1) * 1000).toInt() }
) ?: throw MalformedInputException("Can't extract size")

fun Element.formatFrom(
  cssQuery: String = ":root",
  fullProse: Boolean = true,
  disallowed: List<Format> = emptyList()
) = textFrom(cssQuery).formatFrom(fullProse, disallowed)
fun String.formatFrom(
  fullProse: Boolean = true,
  disallowed: List<Format> = emptyList()
): Format? {
  if (KEG !in disallowed) {
    if (containsWord("keg")) {
      return KEG
    }
  }
  if (CAN !in disallowed) {
    if (
      containsWord("cans") ||
      contains(
        if (fullProse) "\\d+\\s*ml\\s*can".toRegex(IGNORE_CASE) else "can".toRegex(IGNORE_CASE)
      )
    ) {
      return CAN
    }
  }
  if (BOTTLE !in disallowed) {
    if (containsWord("bottle", "bottles")) {
      return BOTTLE
    }
  }
  return null
}

fun <K, V> Map<K, V>.grab(key: K) = this[key] ?: throw MalformedInputException("Key not present: ${key}")

const val INT_REGEX = "(\\d+)"
const val DOUBLE_REGEX = "(\\d+(?:\\.\\d+)?)"
