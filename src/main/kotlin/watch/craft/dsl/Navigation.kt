package watch.craft.dsl

import com.fasterxml.jackson.module.kotlin.readValue
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import watch.craft.MalformedInputException
import watch.craft.Scraper.Node
import watch.craft.Scraper.Node.Retrieval
import watch.craft.Scraper.Node.Retrieval.RetrievalContext
import watch.craft.utils.mapper
import watch.craft.utils.memoize
import java.net.URI

data class Root<Context>(
  val url: URI,
  val context: Context
)

typealias Grab<Data> = suspend () -> Data

fun root(url: String) = root(url, Unit)

fun <Context> root(url: String, context: Context) = Root(url.toUri(), context)

fun fromPaginatedRoots(
  vararg roots: Root<Unit>,
  block: suspend (Grab<Document>) -> List<Node>
) = roots.map { followPagination(it.url, block) }

fun fromHtmlRoots(
  vararg roots: Root<Unit>,
  block: suspend (Grab<Document>) -> List<Node>
) = roots.map { multipleFromHtml(null, it.url, block) }

inline fun <reified Data : Any> fromJsonRoots(
  vararg roots: Root<Unit>,
  crossinline block: suspend (Grab<Data>) -> List<Node>
) = roots.map { multipleFromJson(null, it.url, block) }

fun <Context> fromPaginatedRoots(
  vararg roots: Root<Context>,
  block: suspend (Grab<Document>, Context) -> List<Node>
) = roots.map { followPagination(it.url, block.contextify(it.context)) }

fun <Context> fromHtmlRoots(
  vararg roots: Root<Context>,
  block: suspend (Grab<Document>, Context) -> List<Node>
) = roots.map { multipleFromHtml(null, it.url, block.contextify(it.context)) }

inline fun <reified Data : Any, Context> fromJsonRoots(
  vararg roots: Root<Context>,
  crossinline block: suspend (Grab<Data>, Context) -> List<Node>
) = roots.map { multipleFromJson(null, it.url, block.contextify(it.context)) }


private fun followPagination(url: URI, block: suspend (Grab<Document>) -> List<Node>): Node =
  multipleFromHtml(null, url) { data ->
    listOfNotNull(
      data().maybe { urlFrom("[rel=next]") }
        ?.let { followPagination(it, block) }
    ) + block(data)
  }

fun fromHtml(
  name: String? = null,
  url: URI,
  block: suspend (Grab<Document>) -> Node
) = multipleFromHtml(name, url, block.listify())

inline fun <reified Data : Any> fromJson(
  name: String? = null,
  url: URI,
  crossinline block: suspend (Grab<Data>) -> Node
) = multipleFromJson(name, url, block.listify())

fun multipleFromHtml(
  name: String? = null,
  url: URI,
  block: suspend (Grab<Document>) -> List<Node>
) = Retrieval(
  name = name,
  url = url,
  suffix = ".html",
  validate = {
    // Enough to handle e.g. Wander Beyond serving up random Wix placeholder pages
    try {
      Jsoup.parse(String(it)).selectFrom("title")
    } catch (e: Exception) {
      throw MalformedInputException("Can't extract <title>", e)
    }
  },
  block = block.withMemoizedTransform { Jsoup.parse(String(it), url.toString())!! }
)

inline fun <reified Data : Any> multipleFromJson(
  name: String? = null,
  url: URI,
  crossinline block: suspend (Grab<Data>) -> List<Node>
) = Retrieval(
  name,
  url,
  suffix = ".json",
  validate = { Unit },    // TODO
  block = block.withMemoizedTransform { mapper().readValue(it) }
)

inline fun <Data : Any>
  (suspend (Grab<Data>) -> List<Node>).withMemoizedTransform(
  crossinline transform: (ByteArray) -> Data
): suspend RetrievalContext.() -> List<Node> = {
  val memoized = memoize { transform(data()) }
  this@withMemoizedTransform(memoized)
}

inline fun <Data : Any>
  (suspend (Data) -> Node).listify()
  : suspend (Data) -> List<Node> =
  { data: Data -> listOf(this(data)) }

inline fun <reified Data : Any, Context>
  (suspend (Grab<Data>, Context) -> List<Node>).contextify(context: Context)
  : suspend (Grab<Data>) -> List<Node> =
  { data -> this(data, context) }
