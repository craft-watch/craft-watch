package watch.craft.dsl

import org.jsoup.nodes.Document
import watch.craft.Scraper.Node
import watch.craft.Scraper.Node.Multiple
import watch.craft.Scraper.Node.Work.HtmlWork
import watch.craft.Scraper.Node.Work.JsonWork
import java.net.URI

data class Root<Context>(
  val url: URI,
  val context: Context
)

fun root(url: String) = root(url, Unit)

fun forRoots(vararg roots: Root<Unit>, block: (Document) -> List<Node>) =
  forRoots(*roots, block = block.ignoreContext())

fun forJsonRoots(vararg roots: Root<Unit>, block: (Any) -> List<Node>) =
  forJsonRoots(*roots, block = block.ignoreContext())

fun forPaginatedRoots(vararg roots: Root<Unit>, block: (Document) -> List<Node>) =
  forPaginatedRoots(*roots, block = block.ignoreContext())

fun <Context> root(url: String, context: Context) = Root(url.toUri(), context)

fun <Context> forJsonRoots(vararg roots: Root<Context>, block: (Any, Context) -> List<Node>) =
  roots.mapToMultiple { workJson(it.url) { data -> block(data, it.context) } }

fun <Context> forRoots(vararg roots: Root<Context>, block: (Document, Context) -> List<Node>) =
  roots.mapToMultiple { work(it.url) { data -> block(data, it.context) } }

fun <Context> forPaginatedRoots(vararg roots: Root<Context>, block: (Document, Context) -> List<Node>) =
  roots.mapToMultiple { followPagination(it, block) }

private fun <Context> followPagination(root: Root<Context>, block: (Document, Context) -> List<Node>): Node =
  work(root.url) { doc ->
    val next = doc.maybe { urlFrom("[rel=next]") }
    (if (next != null) {
      listOf(followPagination(root.copy(url = next), block))
    } else {
      emptyList()
    }) + block(doc, root.context)
  }

fun work(url: URI, block: (data: Document) -> List<Node>) = work(null, url) { data -> Multiple(block(data)) }
fun work(name: String? = null, url: URI, block: (data: Document) -> Node) = HtmlWork(name, url, block)

fun workJson(url: URI, block: (data: Any) -> List<Node>) = workJson(null, url) { data -> Multiple(block(data)) }
fun workJson(name: String? = null, url: URI, block: (data: Any) -> Node) = JsonWork(name, url, block)

private fun <T> Array<T>.mapToMultiple(block: (T) -> Node) = Multiple(map(block))

private fun <T> ((T) -> List<Node>).ignoreContext() = { content: T, _: Unit -> this(content) }

