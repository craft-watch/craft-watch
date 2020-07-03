package watch.craft.storage

import com.google.cloud.http.HttpTransportOptions
import com.google.cloud.storage.Bucket.BlobTargetOption.doesNotExist
import com.google.cloud.storage.Storage
import com.google.cloud.storage.Storage.BlobListOption.currentDirectory
import com.google.cloud.storage.Storage.BlobListOption.prefix
import com.google.cloud.storage.StorageException
import com.google.cloud.storage.StorageOptions
import watch.craft.FatalScraperException

class GcsObjectStore(
  bucketName: String,
  storage: Storage = createGcsService()
) : ObjectStore {
  private val bucket = storage.get(bucketName)

  override fun write(key: String, content: ByteArray) {
    try {
      bucket.create(key, content, doesNotExist())
    } catch (e: StorageException) {
      if (e.code == 412) {
        throw FileExistsException(key)
      } else {
        throw FatalScraperException("Error writing to GCS", e)
      }
    }
  }

  override fun read(key: String) = try {
    bucket.get(key).getContent()!!
  } catch (e: StorageException) {
    if (e.code == 404) {
      throw FileDoesntExistException(key)
    } else {
      throw FatalScraperException("Error reading from GCS", e)
    }
  }

  override fun list(key: String) = try {
    val prefix = key.ensureTrailingSlash()
    bucket.list(prefix(prefix), currentDirectory())
      .iterateAll()
      .map { it.name.removePrefix(prefix).removeSuffix("/") }
  } catch (e: StorageException) {
    if (e.code == 404) {
      throw FileDoesntExistException(key)
    } else {
      throw FatalScraperException("Error reading from GCS", e)
    }
  }

  private fun String.ensureTrailingSlash() = if (endsWith("/")) this else ("${this}/")

  companion object {
    fun createGcsService() = StorageOptions.newBuilder().apply {
      setTransportOptions(HttpTransportOptions.newBuilder().apply {
        setConnectTimeout(60_000)
        setReadTimeout(60_000)
      }.build())
    }.build().service!!
  }
}
