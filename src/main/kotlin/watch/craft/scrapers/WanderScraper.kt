package watch.craft.scrapers

import watch.craft.*
import watch.craft.Scraper.Job.Leaf
import watch.craft.Scraper.ScrapedItem
import java.net.URI

class WanderScraper : Scraper {
  override val brewery = Brewery(
    shortName = "Wander Beyond",
    name = "Wander Beyond Brewing",
    location = "Manchester",
    websiteUrl = URI("https://www.wanderbeyondbrewing.com/")
  )

  override val jobs = forRootUrls(ROOT_URL) { root ->
    root
      .selectFrom("product-list-wrapper".hook())  // Only first one, to avoid merch, etc.
      .selectMultipleFrom("product-list-grid-item".hook())
      .map { el ->
        val name = el.textFrom("product-item-name".hook())

        Leaf(name, el.hrefFrom("a")) { doc ->
          val desc = doc.selectFrom("description".hook())
          val descText = desc.text()

          val mixed = name.contains("mixed", ignoreCase = true)

          ScrapedItem(
            name = name,
            summary = desc.maybe { extractFrom("p", ".+[ \u00A0]-[ \u00A0](.+)") }?.get(1),  // Grotesque heterogeneous space characters
            desc = desc.formattedTextFrom(),
            mixed = mixed,
            sizeMl = descText.sizeMlFrom(),
            abv = descText.extract("(\\d+(\\.\\d+)?)%")[1].toDouble(),
            available = true,
            numItems = descText.maybeExtract("(\\d+)x")?.get(1)?.toIntOrNull() ?: 1,
            price = doc.priceFrom("product-price-wrapper".hook()),
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
