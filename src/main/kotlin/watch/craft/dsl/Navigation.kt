package watch.craft.dsl

import org.jsoup.nodes.Document
import watch.craft.Scraper.Job
import watch.craft.Scraper.Job.More
import java.net.URI

data class Root<T>(
  val url: URI,
  val context: T
)

fun root(url: String) = root(url, Unit)
fun forRoots(vararg roots: Root<Unit>, work: (Document) -> List<Job>) =
  forRoots(*roots, work = work.withDummyContext())
fun forPaginatedRoots(vararg roots: Root<Unit>, work: (Document) -> List<Job>) =
  forPaginatedRoots(*roots, work = work.withDummyContext())

fun <T> root(url: String, context: T) = Root(url.toUri(), context)
fun <T> forRoots(vararg roots: Root<T>, work: (Document, T) -> List<Job>) =
  roots.map { More(it.url) { doc -> work(doc, it.context) } }
fun <T> forPaginatedRoots(vararg roots: Root<T>, work: (Document, T) -> List<Job>) =
  roots.map { followPagination(it, work) }

private fun <T> followPagination(root: Root<T>, work: (Document, T) -> List<Job>): More =
  More(root.url) { doc ->
    val next = doc.maybe { urlFrom("[rel=next]") }
    (if (next != null) {
      listOf(followPagination(root.copy(url = next), work))
    } else {
      emptyList()
    }) + work(doc, root.context)
  }

private fun ((Document) -> List<Job>).withDummyContext() = { doc: Document, _: Unit -> this(doc) }

