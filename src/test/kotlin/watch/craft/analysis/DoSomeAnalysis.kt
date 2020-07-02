package watch.craft.analysis

import com.fasterxml.jackson.module.kotlin.readValue
import watch.craft.INVENTORY_JSON_FILE
import watch.craft.Inventory
import watch.craft.KEYWORDS
import watch.craft.mapper

fun main() {
  val items = mapper().readValue<Inventory>(INVENTORY_JSON_FILE).items

  val categoriser = Categoriser(KEYWORDS)

  val categorised = items.map { it.copy(categories = categoriser.categorise(it)) }

  println("==== NO CATEGORY ====")
  categorised
    .filter { it.categories.isEmpty() }
    .forEach { println(it.name) }

  println("")
  println("==== MULTIPLE CATEGORIES ====")
  categorised
    .filter { (it.categories.size > 1) && (!it.mixed) }
    .forEach { println("${it.name}: ${it.categories}") }
}
