package watch.craft

import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import java.net.URI
import java.net.URISyntaxException
import kotlin.math.round
import kotlin.text.RegexOption.DOT_MATCHES_ALL

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


fun String.extract(regex: String) = maybeExtract(regex)
  ?: throw MalformedInputException("Can't extract regex: ${regex}")
fun String.maybeExtract(regex: String) = regex.toRegex(DOT_MATCHES_ALL).find(this)?.groupValues

fun String.toTitleCase(): String = split(" ").joinToString(" ") {
  if (it in BEER_ACRONYMS) it else it.toLowerCase().capitalize()
}

// I *know* this doesn't really work for floating-point.  But it's good enough for our purposes.
fun Double.divideAsPrice(denominator: Int) = round(100 * this / denominator) / 100

fun String.toUri() = try {
  URI(this)
} catch (e: URISyntaxException) {
  throw MalformedInputException("URL syntax error: ${this}", e)
}

fun <K, V> Map<K, V>.grab(key: K) = maybeGrab(key) ?: throw MalformedInputException("Key not present: ${key}")
fun <K, V> Map<K, V>.maybeGrab(key: K) = this[key]

private val BEER_ACRONYMS = listOf(
  "IPL",
  "IPA",
  "DDH",
  "NEIPA",
  "DIPA",
  "XPA"
)
