package watch.craft

import com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
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

fun Element.normaliseParagraphsFrom(cssQuery: String = ":root") = selectFrom(cssQuery)
  .selectMultipleFrom("p")
  .map { it.text() }
  .filterNot { it.isBlank() }
  .joinToString("\n")

fun Element.priceFrom(cssQuery: String = ":root") = extractFrom(cssQuery, "\\d+(\\.\\d+)?")[0].toDouble()

fun Element.extractFrom(cssQuery: String = ":root", regex: String) = textFrom(cssQuery).extract(regex)
fun Element.maybeExtractFrom(cssQuery: String = ":root", regex: String) = maybeTextFrom(cssQuery)?.maybeExtract(regex)

fun Element.textFrom(cssQuery: String = ":root") = selectFrom(cssQuery).text().trim()
fun Element.maybeTextFrom(cssQuery: String = ":root") = maybeSelectFrom(cssQuery)?.text()?.trim()

fun Element.wholeTextFrom(cssQuery: String = ":root") = selectFrom(cssQuery).wholeText().trim()
fun Element.maybeWholeTextFrom(cssQuery: String = ":root") = maybeSelectFrom(cssQuery)?.wholeText()?.trim()

fun Element.ownTextFrom(cssQuery: String = ":root") = selectFrom(cssQuery).ownText().trim()

fun Element.hrefFrom(cssQuery: String = ":root") = attrFrom(cssQuery, "abs:href").toUri()

fun Element.srcFrom(cssQuery: String = ":root") = attrFrom(cssQuery, "abs:src").toUri()

fun Element.dataSrcFrom(cssQuery: String = ":root") = attrFrom(cssQuery, "abs:data-src").toUri()

fun Element.attrFrom(cssQuery: String = ":root", attr: String) = selectFrom(cssQuery).attr(attr)
  .ifBlank { throw MalformedInputException("Attribute blank or not present: ${attr}") }!!

fun Element.maybeSelectFrom(cssQuery: String): Element? = selectFirst(cssQuery)
fun Element.selectFrom(cssQuery: String) = selectFirst(cssQuery)
  ?: throw MalformedInputException("Element not present: ${cssQuery}")

fun Element.selectMultipleFrom(cssQuery: String) = maybeSelectMultipleFrom(cssQuery)
  .ifEmpty { throw MalformedInputException("Element(s) not present: ${cssQuery}") }
fun Element.maybeSelectMultipleFrom(cssQuery: String): Elements = select(cssQuery)


fun String.extract(regex: String, ignoreCase: Boolean = false) = maybeExtract(regex, ignoreCase)
  ?: throw MalformedInputException("Can't extract regex: ${regex}")
fun String.maybeExtract(regex: String, ignoreCase: Boolean = false) = regex.toRegex(regexOptions(ignoreCase)).find(this)?.groupValues

private fun regexOptions(ignoreCase: Boolean = false) = if (ignoreCase) {
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

fun <K, V> Map<K, V>.grab(key: K) = maybeGrab(key) ?: throw MalformedInputException("Key not present: ${key}")
fun <K, V> Map<K, V>.maybeGrab(key: K) = this[key]

const val INT_REGEX = "(\\d+)"
const val DOUBLE_REGEX = "(\\d+(?:\\.\\d+)?)"
const val ABV_REGEX = "${DOUBLE_REGEX}\\s*%"

private val BEER_ACRONYMS = listOf(
  "IPL",
  "IPA",
  "DDH",
  "NEIPA",
  "DIPA",
  "XPA"
)
