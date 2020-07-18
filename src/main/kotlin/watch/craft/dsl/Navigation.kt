package watch.craft.dsl

import org.jsoup.nodes.Document
import watch.craft.Scraper.Job
import watch.craft.Scraper.Job.More
import java.net.URI

data class UrlAndContext<T>(
  val url: String,
  val context: T
)

fun forRootUrls(vararg urls: URI, work: (Document) -> List<Job>) = urls.map { More(it, work) }

fun <T> forRootUrls(vararg tuples: UrlAndContext<T>, work: (Document, T) -> List<Job>) =
  tuples.map { More(it.url.toUri()) { doc -> work(doc, it.context) } }

fun forPaginatedRootUrl(url: URI, work: (Document) -> List<Job>) = listOf(followPagination(url, work))

private fun followPagination(url: URI, work: (Document) -> List<Job>): More = More(url) { root ->
  val next = root.maybe { urlFrom("link[rel=next]") }
  (if (next != null) {
    listOf(followPagination(next, work))
  } else {
    emptyList()
  }) + work(root)
}
