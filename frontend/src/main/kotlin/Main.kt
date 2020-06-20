import kotlinx.html.a
import kotlinx.html.div
import kotlinx.html.dom.append
import kotlinx.html.h1
import kotlinx.html.p
import kotlin.browser.document

fun main() {
  document.body!!.append.div {
    h1 {
      +"Welcome to NEAPI"
    }
    p {
      +"Link to something gimpy: "
        a("https://github.com/oliver-charlesworth/nespot") {
        +"Gimp"
      }
    }
  }
}
