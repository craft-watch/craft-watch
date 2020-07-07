package watch.craft

import com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.nodes.Node
import org.jsoup.nodes.TextNode
import org.jsoup.select.NodeTraversor
import org.jsoup.select.NodeVisitor
import watch.craft.Scraper.Job
import watch.craft.Scraper.Job.More
import java.net.URI
import java.net.URISyntaxException
import kotlin.math.round
import kotlin.text.RegexOption.DOT_MATCHES_ALL
import kotlin.text.RegexOption.IGNORE_CASE

fun forRootUrls(vararg urls: URI, work: (Document) -> List<Job>) = urls.map { More(it, work) }

inline fun <reified T: Any> Element.jsonFrom(cssQuery: String = ":root") = selectFrom(cssQuery).data().run {
  try {
    jacksonObjectMapper()
      .disable(FAIL_ON_UNKNOWN_PROPERTIES)
      .readValue<T>(this)
  } catch (e: Exception) {
    throw MalformedInputException("Couldn't read JSON data", e)
  }
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

  override fun toString() = sbOut.toString()

  companion object {
    private val COMMIT_NODES = listOf("div", "p", "br")
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

fun Element.priceFrom(cssQuery: String = ":root") = extractFrom(cssQuery, "\\d+(\\.\\d+)?")[0].toDouble()

fun Element.abvFrom(
  cssQuery: String = ":root",
  prefix: String = "",
  noPercent: Boolean = false
) = textFrom(cssQuery).abvFrom(prefix, noPercent)
fun String.abvFrom(
  prefix: String = "",
  optionalPercent: Boolean = false
) = extract(prefix + DOUBLE_REGEX + (if (optionalPercent) "" else "\\s*%"))[1].toDouble()

fun Element.sizeMlFrom(cssQuery: String = ":root") = textFrom(cssQuery).sizeMlFrom()
fun String.sizeMlFrom() = maybe { extract("${INT_REGEX}\\s*ml(?:\\W|$)") }?.let { it[1].toInt() }
  ?: maybe { extract("${INT_REGEX}(?:\\s*|-)litre(?:s?)(?:\\W|$)") }?.let { it[1].toInt() * 1000 }
  ?: maybe { extract("${INT_REGEX}\\s*l(?:\\W|$)") }?.let { it[1].toInt() * 1000 }
  ?: throw MalformedInputException("Can't extract size")

operator fun Element.contains(cssQuery: String) = selectFirst(cssQuery) != null

fun Element.extractFrom(cssQuery: String = ":root", regex: String) = textFrom(cssQuery).extract(regex)

fun Element.textFrom(cssQuery: String = ":root") = selectFrom(cssQuery).text().trim()

fun Element.hrefFrom(cssQuery: String = ":root") = attrFrom(cssQuery, "abs:href").toUri()

fun Element.srcFrom(cssQuery: String = ":root") = attrFrom(cssQuery, "abs:src").toUri()

fun Element.dataSrcFrom(cssQuery: String = ":root") = attrFrom(cssQuery, "abs:data-src").toUri()

fun Element.attrFrom(cssQuery: String = ":root", attr: String) = selectFrom(cssQuery).attr(attr)
  .ifBlank { throw MalformedInputException("Attribute blank or not present: ${attr}") }!!

fun Element.selectFrom(cssQuery: String) = selectFirst(cssQuery)
  ?: throw MalformedInputException("Element not present: ${cssQuery}")

fun Element.selectMultipleFrom(cssQuery: String) = select(cssQuery)!!
  .ifEmpty { throw MalformedInputException("Element(s) not present: ${cssQuery}") }

fun String.extract(regex: String, ignoreCase: Boolean = true) = regex.toRegex(regexOptions(ignoreCase))
  .find(this)?.groupValues
  ?: throw MalformedInputException("Can't extract regex: ${regex}")

private fun regexOptions(ignoreCase: Boolean) = if (ignoreCase) {
  setOf(DOT_MATCHES_ALL, IGNORE_CASE)
} else {
  setOf(DOT_MATCHES_ALL)
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


// I *know* this doesn't really work for floating-point.  But it's good enough for our purposes.
fun Double.divideAsPrice(denominator: Int) = round(100 * this / denominator) / 100

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
  "XPA"
)
