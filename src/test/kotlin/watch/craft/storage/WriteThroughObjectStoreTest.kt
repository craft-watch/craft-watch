package watch.craft.storage

import com.nhaarman.mockitokotlin2.*
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows

class WriteThroughObjectStoreTest {
  private val first = mock<ObjectStore>()
  private val second = mock<ObjectStore>()
  private val store = WriteThroughObjectStore(first, second)

  @Test
  fun `writes to both stores in appropriate order`() {
    write()

    inOrder(first, second) {
      verifyBlocking(second) { write(NICE_KEY, NICE_DATA) }
      verifyBlocking(first) { write(NICE_KEY, NICE_DATA) }
    }
  }

  @Test
  fun `still writes to first store if already present in second`() {
    second.stub {
      onBlocking { write(any(), any()) } doThrow FileExistsException("oh")
    }

    write()

    verifyBlocking(first) { write(NICE_KEY, NICE_DATA) }
  }

  @Test
  fun `throws if already present in both`() {
    first.stub {
      onBlocking { write(any(), any()) } doThrow FileExistsException("oh")
    }
    second.stub {
      onBlocking { write(any(), any()) } doThrow FileExistsException("oh")
    }

    assertThrows<FileExistsException> { write() }
  }

  @Test
  fun `reads from first-level store and not from second if present in first`() {
    first.stub {
      onBlocking { read(NICE_KEY) } doReturn NICE_DATA
    }

    val ret = read()

    assertArrayEquals(NICE_DATA, ret)
    verifyBlocking(second, never()) { read(any()) }
  }

  @Test
  fun `reads and copies from second-level store if not present in first`() {
    first.stub {
      onBlocking { read(NICE_KEY) } doThrow FileDoesntExistException("oh")
    }
    second.stub {
      onBlocking { read(NICE_KEY) } doReturn NICE_DATA
    }

    val ret = read()

    assertArrayEquals(NICE_DATA, ret)
    verifyBlocking(first) { write(NICE_KEY, NICE_DATA) }
  }

  @Test
  fun `propagates exception if not found in either`() {
    first.stub {
      onBlocking { read(NICE_KEY) } doThrow FileDoesntExistException("oh")
    }
    second.stub {
      onBlocking { read(NICE_KEY) } doThrow FileDoesntExistException("oh")
    }

    assertThrows<FileDoesntExistException> { read() }
  }

  @Test
  fun `don't throw if data unexpectedly appears in first-level store when we try to copy it`() {
    first.stub {
      onBlocking { read(NICE_KEY) } doThrow FileDoesntExistException("oh")
    }
    first.stub {
      onBlocking { write(any(), any()) } doThrow FileExistsException("oh")
    }
    second.stub {
      onBlocking { read(NICE_KEY) } doReturn NICE_DATA
    }

    assertDoesNotThrow { read() }
  }

  private fun write() = runBlocking { store.write(NICE_KEY, NICE_DATA) }
  private fun read() = runBlocking { store.read(NICE_KEY) }

  companion object {
    private const val NICE_KEY = "foo"
    private val NICE_DATA = byteArrayOf(1, 2, 3, 4)
  }
}
