package watch.craft.dsl

import org.jsoup.nodes.Document
import watch.craft.Scraper.Job
import watch.craft.Scraper.Job.Leaf
import watch.craft.Scraper.Job.More
import watch.craft.Scraper.ScrapedItem
import watch.craft.Scraper.Work.HtmlWork
import watch.craft.Scraper.Work.JsonWork
import java.net.URI

data class Root<Context>(
  val url: URI,
  val context: Context
)

fun root(url: String) = root(url, Unit)

fun forRoots(vararg roots: Root<Unit>, work: (Document) -> List<Job>) =
  forRoots(*roots, work = work.ignoreContext())

fun forJsonRoots(vararg roots: Root<Unit>, work: (Any) -> List<Job>) =
  forJsonRoots(*roots, work = work.ignoreContext())

fun forPaginatedRoots(vararg roots: Root<Unit>, work: (Document) -> List<Job>) =
  forPaginatedRoots(*roots, work = work.ignoreContext())

fun <Context> root(url: String, context: Context) = Root(url.toUri(), context)

fun <Context> forJsonRoots(vararg roots: Root<Context>, work: (Any, Context) -> List<Job>) =
  roots.map { moreJson(it.url) { doc -> work(doc, it.context) } }

fun <Context> forRoots(vararg roots: Root<Context>, work: (Document, Context) -> List<Job>) =
  roots.map { more(it.url) { doc -> work(doc, it.context) } }

fun <Context> forPaginatedRoots(vararg roots: Root<Context>, work: (Document, Context) -> List<Job>) =
  roots.map { followPagination(it, work) }

private fun <Context> followPagination(root: Root<Context>, work: (Document, Context) -> List<Job>): More =
  more(root.url) { doc ->
    val next = doc.maybe { urlFrom("[rel=next]") }
    (if (next != null) {
      listOf(followPagination(root.copy(url = next), work))
    } else {
      emptyList()
    }) + work(doc, root.context)
  }

fun more(url: URI, work: (data: Document) -> List<Job>) = More(HtmlWork(url, work))
fun moreJson(url: URI, work: (data: Any) -> List<Job>) = More(JsonWork(url, work))
fun leaf(name: String, url: URI, work: (data: Document) -> ScrapedItem) = Leaf(name, HtmlWork(url, work))
fun leafJson(name: String, url: URI, work: (data: Any) -> ScrapedItem) = Leaf(name, JsonWork(url, work))

private fun <T> ((T) -> List<Job>).ignoreContext() = { content: T, _: Unit -> this(content) }

