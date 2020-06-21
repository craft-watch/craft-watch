package choliver.neapi.scrapers

fun String.extract(regex: String) = regex.toRegex().find(this)?.groupValues
