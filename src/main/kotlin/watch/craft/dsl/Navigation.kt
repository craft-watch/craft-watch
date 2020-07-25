package watch.craft.dsl

import org.jsoup.nodes.Document
import watch.craft.Scraper.Output
import watch.craft.Scraper.Work.HtmlWork
import watch.craft.Scraper.Work.JsonWork
import java.net.URI

data class Root<Context>(
  val url: URI,
  val context: Context
)

fun root(url: String) = root(url, Unit)

fun forRoots(vararg roots: Root<Unit>, block: (Document) -> Output) =
  forRoots(*roots, block = block.ignoreContext())

fun forJsonRoots(vararg roots: Root<Unit>, block: (Any) -> Output) =
  forJsonRoots(*roots, block = block.ignoreContext())

fun forPaginatedRoots(vararg roots: Root<Unit>, block: (Document) -> Output) =
  forPaginatedRoots(*roots, block = block.ignoreContext())

fun <Context> root(url: String, context: Context) = Root(url.toUri(), context)

fun <Context> forJsonRoots(vararg roots: Root<Context>, block: (Any, Context) -> Output) =
  roots.map { moreJson(it.url) { doc -> block(doc, it.context) } }

fun <Context> forRoots(vararg roots: Root<Context>, block: (Document, Context) -> Output) =
  roots.map { more(it.url) { doc -> block(doc, it.context) } }

fun <Context> forPaginatedRoots(vararg roots: Root<Context>, block: (Document, Context) -> Output) =
  roots.map { followPagination(it, block) }

private fun <Context> followPagination(root: Root<Context>, block: (Document, Context) -> Output): Output =
  more(root.url) { doc ->
    val next = doc.maybe { urlFrom("[rel=next]") }
    (if (next != null) {
      listOf(followPagination(root.copy(url = next), block))
    } else {
      emptyList()
    }) + block(doc, root.context)
  }

fun more(url: URI, work: (data: Document) -> List<Job>) = More(HtmlWork(url, work))
fun moreJson(url: URI, work: (data: Any) -> List<Job>) = More(JsonWork(url, work))
fun leaf(name: String, url: URI, work: (data: Document) -> ScrapedItem) = Leaf(name, HtmlWork(url, work))
fun leafJson(name: String, url: URI, work: (data: Any) -> ScrapedItem) = Leaf(name, JsonWork(url, work))

private fun <T> ((T) -> Output).ignoreContext() = { content: T, _: Unit -> this(content) }

