package choliver.neapi

import choliver.neapi.model.Inventory
import kotlinx.html.*
import kotlinx.html.dom.append
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlin.browser.document
import kotlin.browser.window

fun main() {
  window.fetch("/inventory.json")
    .then { it.text() }
    .then {
      val json = Json(JsonConfiguration.Stable)
      // TODO - error handling
      updateDom(json.parse(Inventory.serializer(), it))
    }
}

fun updateDom(inventory: Inventory) {
  document.body!!.append.div {
    table {
      thead {
        tr {
          th { +"Brewery" }
          th { +"Name" }
          th { +"ABV" }
          th { +"Can size" }
          th { +"Price" }
        }
      }
      tbody {
        inventory.items.forEach { item ->
          tr {
            td { +item.brewery }
            td {
              if (item.thumbnailUrl != null) {
                a(item.url) {
                  img(src = item.thumbnailUrl) {
                    width = "100px"
                    height = "100px"
                    style = "vertical-align: middle; margin-right: 10px"
                  }
                }
              }
              a(item.url) {
                +item.name
              }
            }
            td {
              if (item.abv != null) {
                +"${item.abv.asDynamic().toFixed(1)}%"
              } else {
                +"?"
              }
            }
            td {
              if (item.sizeMl != null) {
                +"${item.sizeMl}ml"
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
