package watch.craft.storage

import com.google.cloud.storage.Blob
import com.google.cloud.storage.Bucket
import com.google.cloud.storage.Bucket.BlobTargetOption
import com.google.cloud.storage.Bucket.BlobTargetOption.doesNotExist
import com.google.cloud.storage.Storage
import com.google.cloud.storage.Storage.BlobListOption.prefix
import com.google.cloud.storage.StorageException
import com.nhaarman.mockitokotlin2.*
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.RETURNS_DEEP_STUBS
import watch.craft.FatalScraperException

class GcsObjectStoreTest {
  private val bucket = mock<Bucket>(defaultAnswer = RETURNS_DEEP_STUBS)
  private val storage = mock<Storage> {
    on { get(NICE_BUCKET) } doReturn bucket
  }
  private val store = GcsObjectStore(NICE_BUCKET, storage)

  @Nested
  inner class Write {
    @Test
    fun `writes data to GCS if not already present`() {
      store.write(NICE_KEY, NICE_DATA)

      verify(bucket).create(NICE_KEY, NICE_DATA, doesNotExist())
    }

    @Test
    fun `throws if file already present`() {
      whenever(bucket.create(any(), any(), any<BlobTargetOption>())) doThrow StorageException(412, "Uh oh")

      assertThrows<FileExistsException> {
        store.write(NICE_KEY, NICE_DATA)
      }
    }

    @Test
    fun `throws if unexpected error`() {
      whenever(bucket.create(any(), any(), any<BlobTargetOption>())) doThrow StorageException(401, "Double uh oh")

      assertThrows<FatalScraperException> {
        store.write(NICE_KEY, NICE_DATA)
      }
    }
  }

  @Nested
  inner class Read {
    @Test
    fun `reads data if present`() {
      val blob = mock<Blob> {
        on { getContent() } doReturn NICE_DATA
      }
      whenever(bucket.get(NICE_KEY)) doReturn blob

      assertArrayEquals(NICE_DATA, store.read(NICE_KEY))
    }

    @Test
    fun `throws if file not present`() {
      whenever(bucket.get(NICE_KEY)) doThrow StorageException(404, "Uh oh")

      assertThrows<FileDoesntExistException> {
        store.read(NICE_KEY)
      }
    }

    @Test
    fun `throws if unexpected error`() {
      whenever(bucket.get(NICE_KEY)) doThrow StorageException(401, "Double uh oh")

      assertThrows<FatalScraperException> {
        store.read(NICE_KEY)
      }
    }
  }

  @Nested
  inner class List {
    @Test
    fun `lists directory contents if present`() {
      val blobAbc = mock<Blob> { on { name } doReturn "foo/abc/" }
      val blobDef = mock<Blob> { on { name } doReturn "foo/def/" }
      whenever(bucket.list(eq(prefix("foo/")), any()).iterateAll()) doReturn listOf(blobAbc, blobDef)

      assertEquals(listOf("abc", "def"), store.list(NICE_KEY))
    }

    @Test
    fun `throws if file not present`() {
      whenever(bucket.list(anyVararg())) doThrow StorageException(404, "Uh oh")

      assertThrows<FileDoesntExistException> {
        store.list(NICE_KEY)
      }
    }

    @Test
    fun `throws if unexpected error`() {
      whenever(bucket.list(anyVararg())) doThrow StorageException(401, "Double uh oh")

      assertThrows<FatalScraperException> {
        store.list(NICE_KEY)
      }
    }
  }

  companion object {
    private const val NICE_BUCKET = "bucket"
    private const val NICE_KEY = "foo"
    private val NICE_DATA = byteArrayOf(1, 2, 3, 4)
  }
}
