package choliver.neapi

class Executor(private val getter: HttpGetter) {
  fun scrapeAll(vararg scrapers: Scraper) = Inventory(
    items = scrapers.flatMap { scraper ->
      with(scraper) {
        val brewery = name
          .trim()
          .validate("non-blank brewery name") { it.isNotBlank() }

        RealScraperContext(getter).scrape().map { it.toItem(brewery) }
      }
    }
  )

  private fun ScrapedItem.toItem(brewery: String) = Item(
    brewery = brewery,
    name = name
      .trim()
      .validate("non-blank item name") { it.isNotBlank() },
    summary = summary
      ?.trim()
      ?.validate("non-blank summary") { it.isNotBlank() },
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
      throw ScraperException("Validation '${name}' failed for value: ${this}")
    }
    return this
  }

  companion object {
    private const val MAX_ABV = 14.0
    private const val MAX_PRICE_PER_ML = 10.00 / 440   // A fairly bougie can
  }
}
