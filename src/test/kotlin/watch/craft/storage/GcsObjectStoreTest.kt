package watch.craft.storage

import com.google.cloud.storage.Blob
import com.google.cloud.storage.Bucket
import com.google.cloud.storage.Bucket.BlobTargetOption
import com.google.cloud.storage.Bucket.BlobTargetOption.doesNotExist
import com.google.cloud.storage.Storage
import com.google.cloud.storage.Storage.BlobListOption.prefix
import com.google.cloud.storage.StorageException
import com.nhaarman.mockitokotlin2.*
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.RETURNS_DEEP_STUBS
import watch.craft.FatalScraperException
import java.net.SocketTimeoutException

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
      write()

      verify(bucket).create(NICE_KEY, NICE_DATA, doesNotExist())
    }

    @Test
    fun `throws if file already present`() {
      whenever(bucket.create(any(), any(), any<BlobTargetOption>())) doThrow ALREADY_EXISTS_EXCEPTION

      assertThrows<FileExistsException> { write() }
    }

    @Test
    fun `throws without retries if unexpected error`() {
      whenever(bucket.create(any(), any(), any<BlobTargetOption>())) doThrow AUTH_EXCEPTION

      assertThrows<FatalScraperException> { write() }
      verify(bucket, times(1)).create(any(), any(), any<BlobTargetOption>())
    }

    @Test
    fun `retries if error cause is a timeout`() {
      whenever(bucket.create(any(), any(), any<BlobTargetOption>())) doThrow TIMEOUT_EXCEPTION

      assertThrows<FatalScraperException> { write() }
      verify(bucket, times(5)).create(any(), any(), any<BlobTargetOption>())
    }

    private fun write() = runBlocking { store.write(NICE_KEY, NICE_DATA) }
  }

  @Nested
  inner class Read {
    @Test
    fun `reads data if present`() {
      val blob = mock<Blob> {
        on { getContent() } doReturn NICE_DATA
      }
      whenever(bucket.get(NICE_KEY)) doReturn blob

      assertArrayEquals(NICE_DATA, read())
    }

    @Test
    fun `throws if file not present`() {
      whenever(bucket.get(NICE_KEY)) doReturn null

      assertThrows<FileDoesntExistException> { read() }
    }

    @Test
    fun `throws without retries if unexpected error`() {
      whenever(bucket.get(NICE_KEY)) doThrow AUTH_EXCEPTION

      assertThrows<FatalScraperException> { read() }
      verify(bucket, times(1)).get(NICE_KEY)
    }

    @Test
    fun `retries if error cause is a timeout`() {
      whenever(bucket.get(NICE_KEY)) doThrow TIMEOUT_EXCEPTION

      assertThrows<FatalScraperException> { read() }
      verify(bucket, times(5)).get(NICE_KEY)
    }

    private fun read() = runBlocking { store.read(NICE_KEY) }
  }

  @Nested
  inner class List {
    @Test
    fun `lists directory contents if present`() {
      val blobAbc = mock<Blob> { on { name } doReturn "foo/abc/" }
      val blobDef = mock<Blob> { on { name } doReturn "foo/def/" }
      whenever(bucket.list(eq(prefix("foo/")), any()).iterateAll()) doReturn listOf(blobAbc, blobDef)

      assertEquals(listOf("abc", "def"), list())
    }

    @Test
    fun `throws if file not present`() {
      whenever(bucket.list(anyVararg())) doThrow NOT_FOUND_EXCEPTION

      assertThrows<FileDoesntExistException> { list() }
    }

    @Test
    fun `throws without retries if unexpected error`() {
      whenever(bucket.list(anyVararg())) doThrow AUTH_EXCEPTION

      assertThrows<FatalScraperException> { list() }
      verify(bucket, times(1)).list(anyVararg())
    }

    @Test
    fun `retries if error cause is a timeout`() {
      whenever(bucket.list(anyVararg())) doThrow TIMEOUT_EXCEPTION

      assertThrows<FatalScraperException> { list() }
      verify(bucket, times(5)).list(anyVararg())
    }

    private fun list() = runBlocking { store.list(NICE_KEY) }
  }

  companion object {
    private const val NICE_BUCKET = "bucket"
    private const val NICE_KEY = "foo"
    private val NICE_DATA = byteArrayOf(1, 2, 3, 4)

    private val AUTH_EXCEPTION = StorageException(401, "Get out")
    private val NOT_FOUND_EXCEPTION = StorageException(404, "Uh oh")
    private val ALREADY_EXISTS_EXCEPTION = StorageException(412, "Uh oh")
    private val TIMEOUT_EXCEPTION = StorageException(SocketTimeoutException("No"))
  }
}
