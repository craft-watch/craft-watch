package watch.craft.migrations

import mu.KotlinLogging
import watch.craft.GCS_BUCKET
import watch.craft.storage.GcsObjectStore.Companion.createGcsService

private val logger = KotlinLogging.logger {}

fun main() {
  val gcs = createGcsService()
  val bucket = gcs.get(GCS_BUCKET)

  val batch = gcs.batch()

  bucket.list().iterateAll()
    .filter { it.name.matches("\\d{4}-\\d{2}-\\d{2}/.*".toRegex()) }
    .forEach { blob ->
      logger.info("Processing: ${blob.name}")
      batch.delete(blob.blobId)
    }

  batch.submit()
}
