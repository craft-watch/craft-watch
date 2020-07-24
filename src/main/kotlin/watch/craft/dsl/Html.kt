package watch.craft.dsl

import org.jsoup.nodes.Element
import org.jsoup.nodes.Node
import org.jsoup.nodes.TextNode
import org.jsoup.select.NodeTraversor
import org.jsoup.select.NodeVisitor
import watch.craft.MalformedInputException
import java.net.URI

fun Element.formattedTextFrom(cssQuery: String = ":root") = with(MyVisitor()) {
  NodeTraversor.traverse(this, selectFirst(cssQuery))
  toString()
}

private class MyVisitor : NodeVisitor {
  private val sbOut = StringBuilder()
  private val sbWorking = StringBuilder()

  override fun head(node: Node, depth: Int) {
    when {
      node is TextNode -> sbWorking.append(node.text())
      node.nodeName() in (COMMIT_NODES + DROP_NODES) -> commit() // Commit anything we've seen so far
    }
  }

  override fun tail(node: Node, depth: Int) {
    when {
      depth == 0 -> commit()  // Root node is a special case
      node.nodeName() in COMMIT_NODES -> commit()
      node.nodeName() in DROP_NODES -> drop()
    }
  }

  private fun commit() {
    val para = sbWorking.toString().trim()
    if (para.isNotBlank()) {
      sbOut.append(para)
      sbOut.append("\n")
    }
    sbWorking.clear()
  }

  private fun drop() {
    sbWorking.clear()
  }

  override fun toString() = sbOut.toString().trim()

  companion object {
    private val COMMIT_NODES = listOf("div", "p", "br", "li")
    private val DROP_NODES = listOf("h1", "h2", "h3", "h4", "h5", "h6")
  }
}

operator fun Element.contains(cssQuery: String) = selectFirst(cssQuery) != null

inline fun <reified T : Any> Element.jsonFrom(cssQuery: String = ":root") = selectFrom(cssQuery).data().jsonFrom<T>()

fun Element.containsMatchFrom(cssQuery: String = ":root", regex: String) = textFrom(cssQuery).containsMatch(regex)
fun Element.extractFrom(cssQuery: String = ":root", regex: String) = textFrom(cssQuery).extract(regex)
fun Element.textFrom(cssQuery: String = ":root") = selectFrom(cssQuery).text().trim()
fun Element.urlFrom(
  cssQuery: String = ":root",
  preference: String? = null,
  transform: (String) -> String = { it }
): URI {
  val attrs = if (preference != null) arrayOf("abs:${preference}") else arrayOf("abs:href", "abs:data-src", "abs:src")
  return attrFrom(cssQuery, *attrs)
    .replace("{width}", "200")
    .replace("\\?v=.*".toRegex(), "")
    .run { transform(this) }
    .toUri()
}

fun Element.attrFrom(cssQuery: String = ":root", vararg attrs: String) = with(selectFrom(cssQuery)) {
  attrs.map { attr(it) }.firstOrNull { it.isNotBlank() }
    ?: throw MalformedInputException("Attribute(s) blank or not present: ${attrs.toList()}")
}

fun Element.selectFrom(cssQuery: String) = selectFirst(cssQuery)
  ?: throw MalformedInputException("Element not present: ${cssQuery}")

fun Element.selectMultipleFrom(cssQuery: String) = select(cssQuery)!!
  .ifEmpty { throw MalformedInputException("Element(s) not present: ${cssQuery}") }
