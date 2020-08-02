package watch.craft.executor

import com.fasterxml.jackson.module.kotlin.readValue
import watch.craft.Brewery
import watch.craft.utils.mapper

class Overrider {
  private val model = mapper().readValue<Model>(javaClass.getResourceAsStream("/overrides.json"))

  fun enrich(brewery: Brewery) = brewery.copy(
    new = brewery.new || brewery.id in model.justAdded
  )

  private data class Model(
    val justAdded: List<String>
  )
}
