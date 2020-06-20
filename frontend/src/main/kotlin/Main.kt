import kotlinx.html.*
import kotlinx.html.dom.append
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.list
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlin.browser.document
import kotlin.browser.window

@Serializable
data class Item(
  val brewery: String,
  val name: String,
  val abv: Float?, // TODO
  val price: Float, // TODO
  val available: Boolean,
  val url: String   // TODO
)


fun main() {
  window.fetch("/inventory.json")
    .then { it.text() }
    .then {
      val json = Json(JsonConfiguration.Stable)
      // TODO - error handling
      updateDom(json.parse(Item.serializer().list, it))
    }
}

fun updateDom(inventory: List<Item>) {
  document.body!!.append.div {
    h1 { +"Welcome to NEAPI" }
    table {
      thead {
        tr {
          th { +"Brewery" }
          th { +"Name" }
          th { +"Abv" }
          th { +"Price" }
        }
      }
      tbody {
        inventory.forEach { item ->
          tr {
            td { +item.brewery }
            td { a(item.url) { +item.name } }
            td {
              if (item.abv != null) {
                +"${item.abv.asDynamic().toFixed(1)}%"
              } else {
                +"?"
              }
            }
            td { +"£${item.price.asDynamic().toFixed(2)}" }
          }
        }
      }
    }
  }
}
