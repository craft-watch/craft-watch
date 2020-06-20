import kotlinx.html.*
import kotlinx.html.dom.append
import kotlin.browser.document

fun main() {
  document.body!!.append.div {
    h1 { +"Welcome to NEAPI" }
    table {
      thead {
        tr {
          th { +"Brewery" }
          th { +"Name" }
          th { +"Price" }
        }
      }
      tbody {
        tr {
          td { +"Villages" }
          td { a("https://example.invalid") { +"Rodeo" } }
          td { +"£3.20" }
        }

        tr {
          td { +"Villages" }
          td { a("https://example.invalid") { +"Rafiki" } }
          td { +"£3.30" }
        }

        tr {
          td { +"Howling Hops" }
          td { a("https://example.invalid") { +"Gimp" } }
          td { +"£5.80" }
        }
      }
    }
  }
}
