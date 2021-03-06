package watch.craft

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.module.kotlin.readValue
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import watch.craft.storage.resolve
import watch.craft.utils.mapper
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter.ISO_DATE_TIME

class ResultsManager(private val structure: StorageStructure) {
  private val logger = KotlinLogging.logger {}
  private val mapper = mapper()

  fun write(inventory: Inventory) {
    inventory.logStats()
    runBlocking {
      dir(inventory.metadata.capturedAt).write(INVENTORY_FILENAME, mapper.writeValueAsBytes(inventory))
    }
    CANONICAL_INVENTORY_PATH.parentFile.mkdirs()
    CANONICAL_INVENTORY_PATH.outputStream().use { mapper.writeValue(it, inventory) }
  }

  fun listHistoricalResults(): List<Instant> = runBlocking {
    structure.results.list().map { Instant.from(formatter.parse(it)) }.sorted()
  }

  suspend fun readMinimalHistoricalResult(timestamp: Instant): MinimalInventory {
    val minimal = mapper.readValue<MinimalInventory>(dir(timestamp).read(INVENTORY_FILENAME))
    return when (minimal.version) {
      1 -> minimal
      else -> throw FatalScraperException("Unsupported version: ${minimal.version}")
    }
  }

  private fun dir(timestamp: Instant) = structure.results.resolve(formatter.format(timestamp))

  // TODO - log actual stats once we trust them
  private fun Inventory.logStats() {
    items.groupBy { it.breweryId }
      .forEach { (key, group) -> logger.info("Scraped (${key}): ${group.size}") }
    logger.info("Scraped (TOTAL): ${items.size}")
  }

  private val formatter = ISO_DATE_TIME.withZone(ZoneOffset.UTC)

  data class MinimalInventory(
    val version: Int = 0,
    val metadata: MinimalMetadata = MinimalMetadata(),
    val stats: MinimalStats? = null,
    val items: List<MinimalItem>
  )

  data class MinimalMetadata(
    val ciBranch: String? = null
  )

  data class MinimalItem(
    @JsonAlias("brewery")
    val breweryId: String,
    val name: String
  )

  data class MinimalStats(
    val breweries: List<Map<String, Any>>
  )
}
