package watch.craft

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.module.kotlin.readValue
import mu.KotlinLogging
import watch.craft.storage.SubObjectStore
import watch.craft.utils.mapper
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter.ISO_DATE_TIME

class ResultsManager(private val setup: Setup) {
  private val logger = KotlinLogging.logger {}
  private val mapper = mapper()

  fun write(inventory: Inventory) {
    inventory.logStats()
    dir(inventory.metadata.capturedAt).write(INVENTORY_FILENAME, mapper.writeValueAsBytes(inventory))
    CANONICAL_INVENTORY_PATH.parentFile.mkdirs()
    CANONICAL_INVENTORY_PATH.outputStream().use { mapper.writeValue(it, inventory) }
  }

  fun listHistoricalResults(): List<Instant> = setup.results.list().map { Instant.from(formatter.parse(it)) }

  // TODO - simplify this on 2020-08-02
  fun readMinimalHistoricalResult(timestamp: Instant): MinimalInventory {
    val minimal = mapper.readValue<MinimalInventory>(dir(timestamp).read(INVENTORY_FILENAME))
    if (minimal.version == 1) {
      return minimal
    }
    val scrapers = SCRAPERS.associate { it.brewery.shortName to it.brewery.id }
    return minimal.copy(items = minimal.items.mapNotNull { item ->
      val actualId = scrapers[item.breweryId]
      if (actualId != null) {
        item.copy(breweryId = actualId)
      } else {
        logger.info("Rejecting unknown brewery: ${item.breweryId}")
        null
      }
    })
  }

  private fun dir(timestamp: Instant) = SubObjectStore(setup.results, formatter.format(timestamp))

  // TODO - log actual stats once we trust them
  private fun Inventory.logStats() {
    items.groupBy { it.breweryId }
      .forEach { (key, group) -> logger.info("Scraped (${key}): ${group.size}") }
    logger.info("Scraped (TOTAL): ${items.size}")
  }

  private val formatter = ISO_DATE_TIME.withZone(ZoneOffset.UTC)

  data class MinimalInventory(
    val version: Int = 0,
    val items: List<MinimalItem>
  )

  data class MinimalItem(
    @JsonAlias("brewery")
    val breweryId: String,
    val name: String
  )
}
