package watch.craft.storage

import com.google.cloud.http.HttpTransportOptions
import com.google.cloud.storage.*
import com.google.cloud.storage.Storage.BlobTargetOption.doesNotExist
import watch.craft.FatalScraperException

class GcsObjectStore(
  private val bucketName: String,
  private val storage: Storage = createGcsService()
) : ObjectStore {
  override fun write(key: String, content: ByteArray) {
    try {
      storage.create(blobInfo(key), content, doesNotExist())
    } catch (e: StorageException) {
      if (e.code == 412) {
        throw FileExistsException(key)
      } else {
        throw FatalScraperException("Error writing to GCS", e)
      }
    }
  }

  override fun read(key: String) = try {
    storage.readAllBytes(blobId(key))!!
  } catch (e: StorageException) {
    if (e.code == 404) {
      throw FileDoesntExistException(key)
    } else {
      throw FatalScraperException("Error reading from GCS", e)
    }
  }

  private fun blobInfo(key: String) = BlobInfo.newBuilder(blobId(key)).build()

  private fun blobId(key: String) = BlobId.of(bucketName, key)

  companion object {
    fun createGcsService() = StorageOptions.newBuilder().apply {
      setTransportOptions(HttpTransportOptions.newBuilder().apply {
        setConnectTimeout(60_000)
        setReadTimeout(60_000)
      }.build())
    }.build().service
  }
}
