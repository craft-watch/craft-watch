package watch.craft.dsl

import com.fasterxml.jackson.module.kotlin.readValue
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import watch.craft.MalformedInputException
import watch.craft.Scraper.Node
import watch.craft.Scraper.Node.Retrieval
import watch.craft.utils.mapper
import java.net.URI

data class Root<Context>(
  val url: URI,
  val context: Context
)

fun root(url: String) = root(url, Unit)

fun <Context> root(url: String, context: Context) = Root(url.toUri(), context)

fun fromPaginatedRoots(
  vararg roots: Root<Unit>,
  block: (Document) -> List<Node>
) = roots.map { followPagination(it.url, block) }

fun fromHtmlRoots(
  vararg roots: Root<Unit>,
  block: (Document) -> List<Node>
) = roots.map { multipleFromHtml(null, it.url, block) }

inline fun <reified Data> fromJsonRoots(
  vararg roots: Root<Unit>,
  crossinline block: (Data) -> List<Node>
) = roots.map { multipleFromJson(null, it.url, block) }

fun <Context> fromPaginatedRoots(
  vararg roots: Root<Context>,
  block: (Document, Context) -> List<Node>
) = roots.map { followPagination(it.url, block.contextify(it.context)) }

fun <Context> fromHtmlRoots(
  vararg roots: Root<Context>,
  block: (Document, Context) -> List<Node>
) = roots.map { multipleFromHtml(null, it.url, block.contextify(it.context)) }

inline fun <reified Data, Context> fromJsonRoots(
  vararg roots: Root<Context>,
  crossinline block: (Data, Context) -> List<Node>
) = roots.map { multipleFromJson(null, it.url, block.contextify(it.context)) }


private fun followPagination(url: URI, block: (Document) -> List<Node>): Node =
  multipleFromHtml(null, url) { data ->
    listOfNotNull(
      data.maybe { urlFrom("[rel=next]") }
        ?.let { followPagination(it, block) }
    ) + block(data)
  }

fun fromHtml(
  name: String? = null,
  url: URI,
  block: (data: Document) -> Node
) = multipleFromHtml(name, url, block.listify())

inline fun <reified Data> fromJson(
  name: String? = null,
  url: URI,
  crossinline block: (data: Data) -> Node
) = multipleFromJson(name, url, block.listify())

fun multipleFromHtml(
  name: String? = null,
  url: URI,
  block: (data: Document) -> List<Node>
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
  block = { block(Jsoup.parse(String(it), url.toString())!!) }
)

inline fun <reified Data> multipleFromJson(
  name: String? = null,
  url: URI,
  crossinline block: (data: Data) -> List<Node>
) = Retrieval(
  name,
  url,
  suffix = ".json",
  validate = { Unit },    // TODO
  block = { block(mapper().readValue(it)) }
)

inline fun <reified Data> ((Data) -> Node).listify() =
  { data: Data -> listOf(this(data)) }

inline fun <reified Data, Context> ((Data, Context) -> List<Node>).contextify(context: Context) =
  { data: Data -> this(data, context) }
