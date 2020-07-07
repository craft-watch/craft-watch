package watch.craft

import com.fasterxml.jackson.module.kotlin.readValue
import watch.craft.storage.SubObjectStore
import watch.craft.utils.mapper
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

class ResultsManager(private val setup: Setup) {
  private val mapper = mapper()

  fun write(inventory: Inventory) {
    dir(inventory.metadata.capturedAt).write(INVENTORY_FILENAME, mapper.writeValueAsBytes(inventory))
    CANONICAL_INVENTORY_PATH.outputStream().use { mapper.writeValue(it, inventory) }
    // TODO - log
  }

  fun listHistoricalResults(): List<Instant> = setup.structure.results.list().map { Instant.from(formatter.parse(it)) }

  fun readMinimalHistoricalResult(timestamp: Instant) =
    mapper.readValue<MinimalInventory>(dir(timestamp).read(INVENTORY_FILENAME))

  private fun dir(timestamp: Instant) =
    SubObjectStore(setup.structure.results, formatter.format(timestamp))

  private val formatter = DateTimeFormatter.ISO_DATE_TIME.withZone(ZoneOffset.UTC)
}
