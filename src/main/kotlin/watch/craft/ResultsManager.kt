package watch.craft

import watch.craft.storage.SubObjectStore
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

class ResultsManager(private val setup: Setup) {
  private val mapper = mapper()

  fun write(inventory: Inventory) {
    val dir = SubObjectStore(setup.structure.results, inventory.metadata.capturedAt.format())
    dir.write("inventory.json", mapper.writeValueAsBytes(inventory))
    INVENTORY_JSON_FILE.outputStream().use { mapper.writeValue(it, inventory) }
    // TODO - log
  }

  private fun Instant.format() =
    DateTimeFormatter.ISO_DATE_TIME.withZone(ZoneOffset.UTC).format(this)

  // TODO - read minimal inventory from date
}
