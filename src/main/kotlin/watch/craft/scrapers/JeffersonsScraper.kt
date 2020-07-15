package watch.craft.scrapers

import watch.craft.Offer
import watch.craft.Scraper
import watch.craft.Scraper.Job.Leaf
import watch.craft.Scraper.ScrapedItem
import watch.craft.utils.*
import java.net.URI

class JeffersonsScraper : Scraper {
  override val jobs = forRootUrls(ROOT_URL) { root ->
    root
      .selectMultipleFrom(".grid--view-items .grid__item")
      .map { el ->
        val rawName = el.textFrom(".grid-view-item__title")

        Leaf(rawName, el.hrefFrom(".grid-view-item__link")) { doc ->
          val desc = doc.formattedTextFrom(".product-single__description")
          val abv = desc.orSkip("No ABV found, so assume not a beer") { abvFrom() }
          val parts = rawName.extract("(.*?)\\s*-\\s*([\\w\\s]+)")

          ScrapedItem(
            name = parts.stringFrom(1),
            summary = parts.stringFrom(2),
            desc = desc,
            abv = abv,
            available = true,
            offers = setOf(
              Offer(
                quantity = desc.extract("(\\d+)\\s*x").intFrom(1),
                totalPrice = el.priceFrom(".product-price__price"),
                sizeMl = desc.sizeMlFrom()
              )
            ),
            thumbnailUrl = el.srcFrom(".grid-view-item__image")
          )
        }
      }
  }

  companion object {
    private val ROOT_URL = URI("https://jeffersonsbrewery.co.uk/")
  }
}
