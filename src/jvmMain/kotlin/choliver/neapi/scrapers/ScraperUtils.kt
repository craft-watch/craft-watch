package choliver.neapi.scrapers

import org.jsoup.nodes.Element

fun String.extract(regex: String) = regex.toRegex().find(this)?.groupValues

fun String.toTitleCase(): String = toLowerCase().split(" ").joinToString(" ") { it.capitalize() }

fun Element.textOf(cssQuery: String) = selectFirst(cssQuery).text().trim()

fun Element.hrefOf(cssQuery: String) = selectFirst(cssQuery).attr("href").trim()

fun Element.srcOf(cssQuery: String) = selectFirst(cssQuery).attr("src").trim()
