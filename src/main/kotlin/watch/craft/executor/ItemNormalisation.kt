package watch.craft.executor

import mu.KotlinLogging
import watch.craft.DEFAULT_SIZE_ML
import watch.craft.InvalidItemException
import watch.craft.Item
import watch.craft.executor.ScraperAdapter.Result

private val logger = KotlinLogging.logger {}

fun StatsWith<Result>.normaliseToItems(): StatsWith<Item> {
  var numInvalid = stats.numInvalid!!
  val entries = entries.mapNotNull { result ->
    try {
      result.normaliseToItem()
    } catch (e: InvalidItemException) {
      logger.warn("[${result.breweryId}] Invalid item [${result.item.name}]", e)
      numInvalid++
      null
    }
  }
  return StatsWith(
    entries,
    stats.copy(numInvalid = numInvalid)
  )
}

private fun Result.normaliseToItem() = Item(
  breweryId = breweryId,
  name = item.name
    .trim()
    .validate("non-blank item name") { it.isNotBlank() },
  summary = item.summary
    ?.trim()
    ?.validate("non-blank summary") { it.isNotBlank() },
  desc = item.desc
    ?.trim()
    ?.validate("non-blank description") { it.isNotBlank() },
  mixed = item.mixed,
  abv = item.abv
    ?.validate("sane ABV") { it <= MAX_ABV },
  // TODO - validate sane size
  offers = item.offers
    .toList()
    .validate("at least one offer") { it.isNotEmpty() }
    .validate("sane price per ml") {
      it.all { offer ->
        (offer.totalPrice / offer.quantity / (offer.sizeMl ?: DEFAULT_SIZE_ML)) <= MAX_PRICE_PER_ML
      }
    },
  available = item.available,
  thumbnailUrl = item.thumbnailUrl
    .validate("absolute thumbnail URL") { it.isAbsolute },
  url = (item.url ?: sourceUrl)
    .validate("absolute URL") { it.isAbsolute }
)

private fun <T> T.validate(name: String, predicate: (T) -> Boolean): T {
  if (!predicate(this)) {
    throw InvalidItemException("Validation '${name}' failed for value: ${this}")
  }
  return this
}

private const val MAX_ABV = 14.0
private const val MAX_PRICE_PER_ML = 30.00 / 750   // A fairly bougie item (Wild Beer "Yadokai Unique Edition")
