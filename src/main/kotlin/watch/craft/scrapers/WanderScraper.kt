package watch.craft.scrapers

import watch.craft.Offer
import watch.craft.Scraper
import watch.craft.Scraper.Job.Leaf
import watch.craft.Scraper.ScrapedItem
import watch.craft.utils.*
import java.net.URI

class WanderScraper : Scraper {
  override val jobs = forRootUrls(ROOT_URL) { root ->
    root
      .selectFrom("product-list-wrapper".hook())  // Only first one, to avoid merch, etc.
      .selectMultipleFrom("product-list-grid-item".hook())
      .map { el ->
        val name = el.textFrom("product-item-name".hook())

        Leaf(name, el.urlFrom("a")) { doc ->
          val desc = doc.selectFrom("description".hook())
          val descText = desc.text()

          val mixed = name.contains("mixed", ignoreCase = true)

          ScrapedItem(
            name = name,
            summary = desc.maybe {
              extractFrom(
                "p",
                ".+[ \u00A0]-[ \u00A0](.+)"
              )[1]
            },  // Grotesque heterogeneous space characters
            desc = desc.formattedTextFrom(),
            mixed = mixed,
            abv = descText.abvFrom(),
            available = true,
            offers = setOf(
              Offer(
                quantity = descText.maybe { extract("(\\d+)x").intFrom(1) } ?: 1,
                totalPrice = doc.priceFrom("product-price-wrapper".hook()),
                sizeMl = descText.sizeMlFrom()
              )
            ),
            thumbnailUrl = URI(
              el.attrFrom("product-item-images".hook(), "style")
                .extract("background-image:url\\((.*?)\\)")[1]
            )
          )
        }
      }
  }

  private fun String.hook() = "[data-hook=${this}]"

  companion object {
    private val ROOT_URL = URI("https://www.wanderbeyondbrewing.com/shop")
  }
}
