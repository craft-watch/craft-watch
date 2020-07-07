package watch.craft.migrations

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.google.cloud.storage.Blob
import mu.KotlinLogging
import watch.craft.GCS_BUCKET
import watch.craft.utils.extract
import watch.craft.utils.mapper
import watch.craft.storage.GcsObjectStore.Companion.createGcsService
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

private val logger = KotlinLogging.logger {}

fun main() {
  val mapper = mapper()
  val gcs = createGcsService()
  val bucket = gcs.get(GCS_BUCKET)

  bucket.list().iterateAll()
    .filter { it.name.endsWith("inventory.json") }
    .forEach { blob ->
      logger.info("Processing: ${blob.name}")

      val timestamp = try {
        extractTimestampFromData(mapper, blob)
          .also { logger.info("Extracted timestamp from data: ${it}") }
      } catch (e: Exception) {
        extractTimestampFromKey(blob)
          .also { logger.info("Extracted timestamp from blob key: ${it}") }
      }

      val targetKey = "results/${timestamp.format()}/inventory.json"
      logger.info("Copying to: ${targetKey}")
      blob.copyTo(GCS_BUCKET, targetKey)
    }
}

private fun extractTimestampFromData(mapper: ObjectMapper, blob: Blob): Instant {
  val inventory = mapper.readValue<MinimalInventory>(blob.getContent())
  return inventory.metadata.capturedAt
}

private fun extractTimestampFromKey(blob: Blob): Instant {
  val parts = blob.name.extract("(.*?)/results/(.*?)/inventory.json")
  return LocalDate.parse(parts[1])
    .atTime(LocalTime.parse(parts[2]))
    .toInstant(ZoneOffset.UTC)
}

private fun Instant.format() = DateTimeFormatter.ISO_DATE_TIME.withZone(ZoneOffset.UTC).format(this)

private data class MinimalInventory(
  val metadata: MinimalMetadata
)

private data class MinimalMetadata(
  val capturedAt: Instant
)
