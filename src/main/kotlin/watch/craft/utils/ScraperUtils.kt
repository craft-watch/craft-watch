package watch.craft.utils

import com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.nodes.Node
import org.jsoup.nodes.TextNode
import org.jsoup.select.NodeTraversor
import org.jsoup.select.NodeVisitor
import watch.craft.Format.*
import watch.craft.MalformedInputException
import watch.craft.Scraper.Job
import watch.craft.Scraper.Job.More
import watch.craft.SkipItemException
import java.net.URI
import java.net.URISyntaxException
import kotlin.text.RegexOption.DOT_MATCHES_ALL
import kotlin.text.RegexOption.IGNORE_CASE

fun forRootUrls(vararg urls: URI, work: (Document) -> List<Job>) = urls.map { More(it, work) }

fun forPaginatedRootUrl(url: URI, work: (Document) -> List<Job>) = listOf(followPagination(url, work))

private fun followPagination(url: URI, work: (Document) -> List<Job>): More = More(url) { root ->
  val next = root.maybe { urlFrom("link[rel=next]") }
  (if (next != null) {
    listOf(followPagination(next, work))
  } else {
    emptyList()
  }) + work(root)
}

inline fun <reified T : Any> Element.jsonFrom(cssQuery: String = ":root") = selectFrom(cssQuery).data().jsonFrom<T>()
inline fun <reified T : Any> String.jsonFrom() = try {
  jacksonObjectMapper()
    .disable(FAIL_ON_UNKNOWN_PROPERTIES)
    .readValue<T>(this)
} catch (e: Exception) {
  throw MalformedInputException("Couldn't read JSON data", e)
}

fun Element.formattedTextFrom(cssQuery: String = ":root") = with(MyVisitor()) {
  NodeTraversor.traverse(this, selectFirst(cssQuery))
  toString()
}

private class MyVisitor : NodeVisitor {
  private val sbOut = StringBuilder()
  private val sbWorking = StringBuilder()

  override fun head(node: Node, depth: Int) {
    when {
      node is TextNode -> sbWorking.append(node.text())
      node.nodeName() in (COMMIT_NODES + DROP_NODES) -> commit() // Commit anything we've seen so far
    }
  }

  override fun tail(node: Node, depth: Int) {
    when {
      depth == 0 -> commit()  // Root node is a special case
      node.nodeName() in COMMIT_NODES -> commit()
      node.nodeName() in DROP_NODES -> drop()
    }
  }

  private fun commit() {
    val para = sbWorking.toString().trim()
    if (para.isNotBlank()) {
      sbOut.append(para)
      sbOut.append("\n")
    }
    sbWorking.clear()
  }

  private fun drop() {
    sbWorking.clear()
  }

  override fun toString() = sbOut.toString().trim()

  companion object {
    private val COMMIT_NODES = listOf("div", "p", "br", "li")
    private val DROP_NODES = listOf("h1", "h2", "h3", "h4", "h5", "h6")
  }
}

fun <T, R> T.orSkip(message: String, block: T.() -> R) = try {
  block(this)
} catch (e: MalformedInputException) {
  throw SkipItemException(message)
}

fun <T, R> T.maybe(block: T.() -> R) = try {
  block(this)
} catch (e: MalformedInputException) {
  null
}

fun Element.priceFrom(cssQuery: String = ":root") = extractFrom(cssQuery, "\\d+(\\.\\d+)?").doubleFrom(0)

fun Element.abvFrom(
  cssQuery: String = ":root",
  prefix: String = "",
  noPercent: Boolean = false
) = textFrom(cssQuery).abvFrom(prefix, noPercent)

fun String.abvFrom(
  prefix: String = "",
  noPercent: Boolean = false
) = extract(prefix + DOUBLE_REGEX + (if (noPercent) "" else "\\s*%")).doubleFrom(1)

fun Element.sizeMlFrom(cssQuery: String = ":root") = textFrom(cssQuery).sizeMlFrom()
fun String.sizeMlFrom() = maybe { extract("$INT_REGEX\\s*ml(?:\\W|$)").intFrom(1) }
  ?: maybe { extract("$INT_REGEX(?:\\s*|-)(?:litre|liter)(?:s?)(?:\\W|$)").intFrom(1) * 1000 }
  ?: maybe { extract("$INT_REGEX\\s*l(?:\\W|$)").intFrom(1) * 1000 }
  ?: throw MalformedInputException("Can't extract size")

fun List<String>.intFrom(idx: Int) = get(idx).toInt()
fun List<String>.doubleFrom(idx: Int) = get(idx).toDouble()
fun List<String>.stringFrom(idx: Int) = get(idx).trim()

operator fun Element.contains(cssQuery: String) = selectFirst(cssQuery) != null

fun Element.containsMatchFrom(cssQuery: String = ":root", regex: String) = textFrom(cssQuery).containsMatch(regex)
fun Element.extractFrom(cssQuery: String = ":root", regex: String) = textFrom(cssQuery).extract(regex)
fun Element.textFrom(cssQuery: String = ":root") = selectFrom(cssQuery).text().trim()
fun Element.urlFrom(
  cssQuery: String = ":root",
  preference: String? = null,
  transform: (String) -> String = { it }
): URI {
  val attrs = if (preference != null) arrayOf("abs:${preference}") else arrayOf("abs:href", "abs:data-src", "abs:src")
  return attrFrom(cssQuery, *attrs)
    .replace("{width}", "200")
    .replace("\\?v=.*".toRegex(), "")
    .run { transform(this) }
    .toUri()
}

fun Element.attrFrom(cssQuery: String = ":root", vararg attrs: String) = with(selectFrom(cssQuery)) {
  attrs.map { attr(it) }.firstOrNull { it.isNotBlank() }
    ?: throw MalformedInputException("Attribute(s) blank or not present: ${attrs}")
}

fun Element.selectFrom(cssQuery: String) = selectFirst(cssQuery)
  ?: throw MalformedInputException("Element not present: ${cssQuery}")

fun Element.selectMultipleFrom(cssQuery: String) = select(cssQuery)!!
  .ifEmpty { throw MalformedInputException("Element(s) not present: ${cssQuery}") }

fun String.remove(vararg regexes: String) = regexes.fold(this) { str, regex ->
  str.replace(regex.toRegex(regexOptions(true)), "")
}.trim()

fun String.containsWord(vararg words: String) = tokenize()
  .filter { it.wordy }
  .map { it.text.toLowerCase() }
  .toSet()
  .any { it in words }

fun String.containsMatch(regex: String, ignoreCase: Boolean = true) = regex.toRegex(regexOptions(ignoreCase))
  .containsMatchIn(this)

fun String.extract(regex: String, ignoreCase: Boolean = true) = regex.toRegex(regexOptions(ignoreCase))
  .find(this)?.groupValues
  ?: throw MalformedInputException("Can't extract regex: ${regex}")

private fun regexOptions(ignoreCase: Boolean) = if (ignoreCase) {
  setOf(DOT_MATCHES_ALL, IGNORE_CASE)
} else {
  setOf(DOT_MATCHES_ALL)
}

fun Element.formatFrom(cssQuery: String = ":root", fullProse: Boolean = true) = textFrom(cssQuery).formatFrom(fullProse)
fun String.formatFrom(fullProse: Boolean = true) = when {
  containsWord("keg") -> KEG
  contains(
    if (fullProse) "\\d+\\s*ml\\s*can".toRegex(IGNORE_CASE) else "can".toRegex(IGNORE_CASE)
  ) -> CAN   // Can't match directly on "can" because it's a regular English word
  containsWord("cans") -> CAN
  containsWord("bottles") -> BOTTLE
  containsWord("bottle") -> BOTTLE
  else -> null
}

fun String.toTitleCase() = tokenize()
  .joinToString("") { token ->
    if (token.wordy && token.text !in BEER_ACRONYMS) {
      token.text.toLowerCase().capitalize()
    } else {
      token.text
    }
  }

private fun String.tokenize(): List<Token> {
  if (length == 0) {
    return emptyList()
  }

  fun wordy(c: Char) = Character.isAlphabetic(c.toInt()) || (c == '\'')

  val chars = toCharArray()
  val tokens = mutableListOf<Token>()
  var iStart = 0
  var wordy = wordy(chars[0])
  for (i in 1 until length) {
    val nextWordy = wordy(chars[i])
    if (nextWordy != wordy) {
      tokens.add(Token(substring(iStart, i), wordy))
      iStart = i
    }
    wordy = nextWordy
  }
  tokens.add(Token(substring(iStart, length), wordy))
  return tokens
}

private data class Token(
  val text: String,
  val wordy: Boolean
)

fun String.toUri() = try {
  URI(this)
} catch (e: URISyntaxException) {
  throw MalformedInputException("URL syntax error: ${this}", e)
}

fun <K, V> Map<K, V>.grab(key: K) = this[key] ?: throw MalformedInputException("Key not present: ${key}")

const val INT_REGEX = "(\\d+)"
const val DOUBLE_REGEX = "(\\d+(?:\\.\\d+)?)"

private val BEER_ACRONYMS = listOf(
  "IPL",
  "IPA",
  "DDH",
  "NEIPA",
  "DIPA",
  "NEDIPA",
  "XPA"
)
