package watch.craft.scrapers

import org.jsoup.nodes.Document
import watch.craft.*
import watch.craft.Scraper.*
import watch.craft.Scraper.Job.Leaf
import java.net.URI

class NorthernMonkScraper : Scraper {
  override val brewery = Brewery(
    shortName = "Northern Monk",
    name = "Northern Monk Brewing Co",
    location = "Holbeck, Leeds",
    websiteUrl = URI("https://northernmonk.com/")
  )

  override val jobs = forRootUrls(ROOT_URL, work = ::scrapeRoot)

  private fun scrapeRoot(root: Document) = maybeGetNextPageJob(root) + getItemJobs(root)

  private fun maybeGetNextPageJob(root: Document): List<Job> {
    val next = root.maybeHrefFrom("link[rel=next]")
    return if (next != null) {
      forRootUrls(next, work = ::scrapeRoot)
    } else {
      emptyList()
    }
  }

  private fun getItemJobs(root: Document): List<Leaf> {
    return root
      .selectMultipleFrom(".card")
      .map { el ->
        val rawName = el.textFrom(".card__name")

        Leaf(rawName, el.hrefFrom(".card__wrapper")) { doc ->
          ScrapedItem(
            name = rawName,
            summary = null,
            desc = null,
            mixed = false,
            sizeMl = null,
            abv = null,
            available = true,
            numItems = 1,
            price = el.priceFrom(".card__price"),
            thumbnailUrl = URI("https://example.invalid")
          )
        }
      }
  }

  companion object {
    private val ROOT_URL = URI("https://northernmonkshop.com/collections/beer")
  }
}
