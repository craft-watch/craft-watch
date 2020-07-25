package watch.craft.dsl

import org.jsoup.nodes.Document
import watch.craft.Scraper.Output
import watch.craft.Scraper.Output.Multiple
import watch.craft.Scraper.Output.Work.HtmlWork
import watch.craft.Scraper.Output.Work.JsonWork
import java.net.URI

data class Root<Context>(
  val url: URI,
  val context: Context
)

fun root(url: String) = root(url, Unit)

fun forRoots(vararg roots: Root<Unit>, block: (Document) -> List<Output>) =
  forRoots(*roots, block = block.ignoreContext())

fun forJsonRoots(vararg roots: Root<Unit>, block: (Any) -> List<Output>) =
  forJsonRoots(*roots, block = block.ignoreContext())

fun forPaginatedRoots(vararg roots: Root<Unit>, block: (Document) -> List<Output>) =
  forPaginatedRoots(*roots, block = block.ignoreContext())

fun <Context> root(url: String, context: Context) = Root(url.toUri(), context)

fun <Context> forJsonRoots(vararg roots: Root<Context>, block: (Any, Context) -> List<Output>) =
  roots.mapToMultiple { workJson(it.url) { data -> block(data, it.context) } }

fun <Context> forRoots(vararg roots: Root<Context>, block: (Document, Context) -> List<Output>) =
  roots.mapToMultiple { work(it.url) { data -> block(data, it.context) } }

fun <Context> forPaginatedRoots(vararg roots: Root<Context>, block: (Document, Context) -> List<Output>) =
  roots.mapToMultiple { followPagination(it, block) }

private fun <Context> followPagination(root: Root<Context>, block: (Document, Context) -> List<Output>): Output =
  work(root.url) { doc ->
    val next = doc.maybe { urlFrom("[rel=next]") }
    (if (next != null) {
      listOf(followPagination(root.copy(url = next), block))
    } else {
      emptyList()
    }) + block(doc, root.context)
  }

fun work(url: URI, block: (data: Document) -> List<Output>) = work(null, url) { data -> Multiple(block(data)) }
fun work(name: String? = null, url: URI, block: (data: Document) -> Output) = HtmlWork(name, url, block)

fun workJson(url: URI, block: (data: Any) -> List<Output>) = workJson(null, url) { data -> Multiple(block(data)) }
fun workJson(name: String? = null, url: URI, block: (data: Any) -> Output) = JsonWork(name, url, block)

private fun <T> Array<T>.mapToMultiple(block: (T) -> Output) = Multiple(map(block))

private fun <T> ((T) -> List<Output>).ignoreContext() = { content: T, _: Unit -> this(content) }

