package watch.craft.executor

import watch.craft.InvalidItemException
import watch.craft.Item
import watch.craft.divideAsPrice
import watch.craft.executor.ScraperExecutor.Result

fun Result.normalise() = Item(
  brewery = brewery
    .trim()
    .validate("non-blank brewery name") { it.isNotBlank() },
  name = item.name
    .trim()
    .validate("non-blank item name") { it.isNotBlank() },
  summary = item.summary
    ?.trim()
    ?.validate("non-blank summary") { it.isNotBlank() },
  desc = item.desc
    ?.trim()
    ?.validate("non-blank description") { it.isNotBlank() },
  keg = item.keg,
  mixed = item.mixed,
  // TODO - validate sane size
  sizeMl = item.sizeMl,
  abv = item.abv
    ?.validate("sane ABV") { it < MAX_ABV },
  numItems = item.numItems,
  perItemPrice = item.price.divideAsPrice(item.numItems)  // TODO - separate price field
    .validate("sane price per ml") {
      (it / (item.sizeMl ?: 330)) < MAX_PRICE_PER_ML
    },
  available = item.available,
  thumbnailUrl = item.thumbnailUrl
    .validate("absolute thumbnail URL") { it.isAbsolute }
    .toString(),
  url = entry.url
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
private const val MAX_PRICE_PER_ML = 12.00 / 440   // A fairly bougie can
