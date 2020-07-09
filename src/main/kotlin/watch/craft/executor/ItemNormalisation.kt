package watch.craft.executor

import watch.craft.InvalidItemException
import watch.craft.Item
import watch.craft.executor.ScraperAdapter.Result
import watch.craft.utils.toUri

fun Result.normalise() = Item(
  brewery = breweryName
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
  offers = item.offers
    .validate("sane price per ml") {
      it.all { offer ->
        (offer.totalPrice / offer.quantity / (item.sizeMl ?: 330)) < MAX_PRICE_PER_ML
      }
    },
  available = item.available,
  thumbnailUrl = item.thumbnailUrl
    .validate("absolute thumbnail URL") { it.isAbsolute }
    .toString()
    .replace("\\?v=.*".toRegex(), "")
    .toUri(),
  url = url
    .validate("absolute URL") { it.isAbsolute }
)

private fun <T> T.validate(name: String, predicate: (T) -> Boolean): T {
  if (!predicate(this)) {
    throw InvalidItemException("Validation '${name}' failed for value: ${this}")
  }
  return this
}

private const val MAX_ABV = 14.0
private const val MAX_PRICE_PER_ML = 12.00 / 440   // A fairly bougie can
