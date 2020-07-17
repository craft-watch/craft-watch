package watch.craft

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
    // TODO - log
  }

  fun listHistoricalResults(): List<Instant> = setup.results.list().map { Instant.from(formatter.parse(it)) }

  fun readMinimalHistoricalResult(timestamp: Instant) =
    mapper.readValue<MinimalInventory>(dir(timestamp).read(INVENTORY_FILENAME))

  private fun dir(timestamp: Instant) = SubObjectStore(setup.results, formatter.format(timestamp))

  // TODO - log actual stats once we trust them
  private fun Inventory.logStats() {
    items.groupBy { it.breweryId }
      .forEach { (key, group) -> logger.info("Scraped (${key}): ${group.size}") }
    logger.info("Scraped (TOTAL): ${items.size}")
  }

  private val formatter = ISO_DATE_TIME.withZone(ZoneOffset.UTC)
}
