package watch.craft.executor

import com.fasterxml.jackson.module.kotlin.convertValue
import kotlinx.coroutines.runBlocking
import watch.craft.*
import watch.craft.ResultsManager.MinimalInventory
import watch.craft.utils.ZONE_OFFSET
import watch.craft.utils.mapper
import java.time.Instant
import java.time.LocalDate

fun Inventory.addBaselineStats(results: ResultsManager, now: Instant): Inventory {
  return copy(
    incubating = Incubating(
      baselineStats = baselineInventory(results, now)
        ?.extractStats(breweries.map { it.id })
        ?: Stats()
    )
  )
}

private fun baselineInventory(
  results: ResultsManager,
  now: Instant
): MinimalInventory? {
  val startOfDay = LocalDate.ofInstant(now, ZONE_OFFSET).atStartOfDay().toInstant(ZONE_OFFSET)
  return results.listHistoricalResults()
    .lastOrNull { it < startOfDay }
    ?.let { runBlocking { results.readMinimalHistoricalResult(it) } }
}

private fun MinimalInventory.extractStats(ids: List<String>) = if (stats == null) {
  Stats()
} else {
  Stats(
    breweries = stats.breweries.mapNotNull {
      if (it["breweryId"] in ids) {
        mapper().convertValue<BreweryStats>(it)
      } else null
    }
  )
}
