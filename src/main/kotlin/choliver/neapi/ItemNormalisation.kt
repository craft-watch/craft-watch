package choliver.neapi

import java.net.URI

fun Scraper.Item.normalise(brewery: String, url: URI) = Item(
  brewery = brewery
    .trim()
    .validate("non-blank brewery name") { it.isNotBlank() },
  name = name
    .trim()
    .validate("non-blank item name") { it.isNotBlank() },
  summary = summary
    ?.trim()
    ?.validate("non-blank summary") { it.isNotBlank() },
  desc = desc
    ?.trim()
    ?.validate("non-blank description") { it.isNotBlank() },
  keg = keg,
  mixed = mixed,
  // TODO - validate sane size
  sizeMl = sizeMl,
  abv = abv
    ?.validate("sane ABV") { it < MAX_ABV },
  perItemPrice = perItemPrice
    .validate("sane price per ml") { (it / (sizeMl ?: 330)) < MAX_PRICE_PER_ML },
  available = available,
  thumbnailUrl = thumbnailUrl
    .validate("absolute thumbnail URL") { it.isAbsolute }
    .toString(),
  url = url
    .validate("absolute URL") { it.isAbsolute }
    .toString()
)

private fun <T> T.validate(name: String, predicate: (T) -> Boolean): T {
  if (!predicate(this)) {
    throw InvalidItemException("Validation '${name}' failed for value: ${this}")
  }
  return this
}

private const val MAX_ABV = 14.0
private const val MAX_PRICE_PER_ML = 10.00 / 440   // A fairly bougie can
