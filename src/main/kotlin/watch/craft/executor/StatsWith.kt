package watch.craft.executor

import watch.craft.BreweryStats

data class StatsWith<T>(
  val entries: List<T>,
  val stats: BreweryStats
)
