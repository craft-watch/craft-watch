package watch.craft.dsl

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import watch.craft.MalformedInputException
import watch.craft.SkipItemException
import java.net.URI
import java.net.URISyntaxException

fun String.skipIfOnBlacklist(vararg blacklist: String) {
  if (containsWord(*blacklist)) {
    throw SkipItemException("Contains blacklisted term ${blacklist.toList()}")
  }
}

inline fun <reified T : Any> String.jsonFrom() = try {
  jacksonObjectMapper()
    .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
    .readValue<T>(this)
} catch (e: Exception) {
  throw MalformedInputException("Couldn't read JSON data", e)
}

fun String.toUri() = try {
  URI(this)
} catch (e: URISyntaxException) {
  throw MalformedInputException("URL syntax error: ${this}", e)
}

fun String.cleanse(vararg regexesToRemove: String) = regexesToRemove.fold(this) { str, regex ->
  str.replace(regex.toRegex(regexOptions(true)), "")
}.replace("\\s+".toRegex(), " ").trim()

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

fun List<String>.intFrom(idx: Int) = get(idx).toInt()
fun List<String>.doubleFrom(idx: Int) = get(idx).toDouble()
fun List<String>.stringFrom(idx: Int) = get(idx).trim()

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

fun <R : Any> String.collectFromLines(extract: String.() -> R) = split("\n").mapNotNull { it.maybe(extract) }

private fun regexOptions(ignoreCase: Boolean) = if (ignoreCase) {
  setOf(RegexOption.DOT_MATCHES_ALL, RegexOption.IGNORE_CASE)
} else {
  setOf(RegexOption.DOT_MATCHES_ALL)
}

private val BEER_ACRONYMS = listOf(
  "IPL",
  "IPA",
  "DDH",
  "NEIPA",
  "DIPA",
  "NEDIPA",
  "XPA"
)

