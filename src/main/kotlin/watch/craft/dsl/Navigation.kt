package watch.craft.dsl

import com.fasterxml.jackson.module.kotlin.readValue
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import watch.craft.MalformedInputException
import watch.craft.Scraper.Node
import watch.craft.Scraper.Node.Multiple
import watch.craft.Scraper.Node.Work
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
) = roots.mapToMultiple {
  followPagination(it.url, block)
}

fun fromHtmlRoots(
  vararg roots: Root<Unit>,
  block: (Document) -> List<Node>
) = roots.mapToMultiple {
  fromHtml(null, it.url) { data -> Multiple(block(data)) }
}

fun fromJsonRoots(
  vararg roots: Root<Unit>,
  block: (Any) -> List<Node>
) = roots.mapToMultiple {
  fromJson(null, it.url) { data -> Multiple(block(data)) }
}

fun <Context> fromPaginatedRoots(
  vararg roots: Root<Context>,
  block: (Document, Context) -> List<Node>
) = roots.mapToMultiple {
  followPagination(it.url) { data -> block(data, it.context) }
}

fun <Context> fromHtmlRoots(
  vararg roots: Root<Context>,
  block: (Document, Context) -> List<Node>
) = roots.mapToMultiple {
  fromHtml(null, it.url) { data -> Multiple(block(data, it.context)) }
}

fun <Context> fromJsonRoots(
  vararg roots: Root<Context>,
  block: (Any, Context) -> List<Node>
) = roots.mapToMultiple {
  fromJson(null, it.url) { data -> Multiple(block(data, it.context)) }
}


// Primitives

private fun followPagination(url: URI, block: (Document) -> List<Node>): Node =
  fromHtml(null, url) { data ->
    Multiple(
      listOfNotNull(
        data.maybe { urlFrom("[rel=next]") }
          ?.let { followPagination(it, block) }
      ) + block(data)
    )
  }

fun fromHtml(
  name: String? = null,
  url: URI,
  block: (data: Document) -> Node
) = Work(
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

fun fromJson(
  name: String? = null,
  url: URI,
  block: (data: Any) -> Node
) = Work(
  name,
  url,
  suffix = ".json",
  validate = { Unit },    // TODO
  block = { block(mapper.readValue(it)) }
)

private val mapper = mapper()

private fun <T> Array<T>.mapToMultiple(block: (T) -> Node) = Multiple(map(block))

private fun <T> ((T) -> List<Node>).ignoreContext() = { content: T, _: Unit -> this(content) }

