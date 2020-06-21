package choliver.neapi.scrapers

import choliver.neapi.ParsedItem
import choliver.neapi.Scraper
import choliver.neapi.Scraper.Context
import java.net.URI

class BoxcarScraper : Scraper {
  override val name = "Boxcar"

  override fun Context.scrape() = request(ROOT_URL)
    .select(".product-card")
    .map { el ->
      val rawName = el.selectFirst(".product-card__title").text()
      val result = "^(.*?) // (.*?)% *(.*?)? // (.*?)ml$".toRegex().find(rawName)!!

      ParsedItem(
        thumbnailUrl = ROOT_URL.resolve(
          el.selectFirst("noscript .grid-view-item__image").attr("src").trim()
            .replace("@2x", "")
            .replace("\\?.*".toRegex(), "")
        ),
        url = ROOT_URL.resolve(el.selectFirst(".grid-view-item__link").attr("href").trim()),
        name = result.groupValues[1].trim(),
        abv = result.groupValues[2].trim().toBigDecimal(),
        summary = result.groupValues[3].trim().ifEmpty { null },
        sizeMl = result.groupValues[4].trim().toInt(),
        available = "price--sold-out" !in el.selectFirst(".price").classNames(),
        pricePerCan = el.selectFirst(".price-item--sale")
          .text()
          .trim()
          .removePrefix("£")
          .toBigDecimal()
      )
    }

  companion object {
    private val ROOT_URL = URI("https://shop.boxcarbrewery.co.uk/collections/beer")
  }
}
