package watch.craft.scrapers

import org.jsoup.nodes.Document
import watch.craft.*
import watch.craft.Scraper.Job
import watch.craft.Scraper.Job.Leaf
import watch.craft.Scraper.ScrapedItem
import java.net.URI
import kotlin.text.RegexOption.IGNORE_CASE

class NorthernMonkScraper : Scraper {
  override val brewery = Brewery(
    shortName = "Northern Monk",
    name = "Northern Monk Brew Co",
    location = "Holbeck, Leeds",
    websiteUrl = URI("https://northernmonk.com/")
  )

  override val jobs = forRootUrls(ROOT_URL, work = ::scrapeRoot)

  // TODO - absorb this pattern into forRootUrls
  private fun scrapeRoot(root: Document) = maybeGetNextPageJob(root) + getItemJobs(root)

  private fun maybeGetNextPageJob(root: Document): List<Job> {
    val next = root.maybeHrefFrom("link[rel=next]")
    return if (next != null) {
      forRootUrls(next, work = ::scrapeRoot)
    } else {
      emptyList()
    }
  }

  // TODO - use change in caps style to identify more summaries

  private fun getItemJobs(root: Document): List<Leaf> {
    return root
      .selectMultipleFrom(".card")
      .map { el ->
        val rawName = el.textFrom(".card__name").toTitleCase()

        Leaf(rawName, el.hrefFrom(".card__wrapper")) { doc ->
          val desc = doc.selectFrom(".product__description")
          val abv = desc.maybeExtractFrom(regex = ABV_REGEX)?.get(1)?.toDouble()
          val sizeMl = desc.maybeExtractFrom(regex = SIZE_REGEX)?.get(1)?.toInt()
          val mixed = desc.children()
            .count { it.text().contains(ITEM_MULTIPLE_REGEX.toRegex(IGNORE_CASE)) } > 1

          if (abv == null && !mixed) {
            throw SkipItemException("Assume that lack of ABV for non-mixed means not a beer product")
          }

          ScrapedItem(
            name = rawName
              .replace(PACK_REGEX.toRegex(IGNORE_CASE), "")
              .split("//")[0]
              .trim(),
            summary = rawName.maybeExtract("[^/]+\\s+//\\s+(.*)")?.get(1),
            desc = desc.normaliseParagraphsFrom(),
            mixed = mixed,
            sizeMl = sizeMl,
            abv = abv,
            available = true,
            numItems = rawName.maybeExtract(PACK_REGEX, ignoreCase = true)?.get(1)?.toInt() ?: 1,
            price = el.priceFrom(".card__price"),
            thumbnailUrl = URI(
              // The URLs are dynamically created
              doc.attrFrom(".product__image.lazyload", "abs:data-src")
                .replace("{width}", "180")
            )
          )
        }
      }
  }

  companion object {
    private val ROOT_URL = URI("https://northernmonkshop.com/collections/beer")

    private const val PACK_REGEX = "(\\d+) pack"
    private const val ITEM_MULTIPLE_REGEX = "\\d+\\s+x"
  }
}
