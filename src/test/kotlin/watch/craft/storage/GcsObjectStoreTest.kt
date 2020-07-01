package watch.craft.storage

import com.google.cloud.storage.Storage
import com.google.cloud.storage.Storage.BlobTargetOption.doesNotExist
import com.google.cloud.storage.StorageException
import com.nhaarman.mockitokotlin2.*
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import watch.craft.FatalScraperException

class GcsObjectStoreTest {
  private val storage = mock<Storage>()
  private val store = GcsObjectStore(NICE_BUCKET, storage)

  @Test
  fun `writes data to GCS if not already present`() {
    store.write(NICE_KEY, NICE_DATA)

    verify(storage).create(any(), eq(NICE_DATA), eq(doesNotExist()))
  }

  @Test
  fun `throws if file already present`() {
    whenever(storage.create(any(), any<ByteArray>(), any())) doThrow StorageException(412, "Uh oh")

    assertThrows<FileExistsException> {
      store.write(NICE_KEY, NICE_DATA)
    }
  }

  @Test
  fun `throws if unexpected error on write`() {
    whenever(storage.create(any(), any<ByteArray>(), any())) doThrow StorageException(401, "Double uh oh")

    assertThrows<FatalScraperException> {
      store.write(NICE_KEY, NICE_DATA)
    }
  }

  @Test
  fun `reads data if present`() {
    whenever(storage.readAllBytes(any())) doReturn NICE_DATA

    assertArrayEquals(NICE_DATA, store.read(NICE_KEY))
  }

  @Test
  fun `throws if file not present`() {
    whenever(storage.readAllBytes(any())) doThrow StorageException(404, "Uh oh")

    assertThrows<FileDoesntExistException> {
      store.read(NICE_KEY)
    }
  }

  @Test
  fun `throws if unexpected error on read`() {
    whenever(storage.readAllBytes(any())) doThrow StorageException(401, "Double uh oh")

    assertThrows<FatalScraperException> {
      store.read(NICE_KEY)
    }
  }

  companion object {
    private const val NICE_BUCKET = "bucket"
    private const val NICE_KEY = "foo"
    private val NICE_DATA = byteArrayOf(1, 2, 3, 4)
  }
}
