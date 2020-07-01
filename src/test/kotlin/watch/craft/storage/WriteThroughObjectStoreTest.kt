package watch.craft.storage

import com.nhaarman.mockitokotlin2.*
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class WriteThroughObjectStoreTest {
  private val first = mock<ObjectStore>()
  private val second = mock<ObjectStore>()
  private val store = WriteThroughObjectStore(first, second)

  @Test
  fun `writes to both stores in appropriate order`() {
    store.write(NICE_KEY, NICE_DATA)

    inOrder(first, second) {
      verify(second).write(NICE_KEY, NICE_DATA)
      verify(first).write(NICE_KEY, NICE_DATA)
    }
  }

  @Test
  fun `writes to first store if already present in second`() {
    whenever(second.write(any(), any())) doThrow FileExistsException("oh")

    store.write(NICE_KEY, NICE_DATA)

    verify(first).write(NICE_KEY, NICE_DATA)
  }

  @Test
  fun `throws if already present in both`() {
    whenever(first.write(any(), any())) doThrow FileExistsException("oh")
    whenever(second.write(any(), any())) doThrow FileExistsException("oh")

    assertThrows<FileExistsException> {
      store.write(NICE_KEY, NICE_DATA)
    }
  }

  @Test
  fun `reads from first-level store and not from second if present in first`() {
    whenever(first.read(NICE_KEY)) doReturn NICE_DATA

    val ret = store.read(NICE_KEY)

    assertArrayEquals(NICE_DATA, ret)
    verify(second, never()).read(any())
  }

  @Test
  fun `reads and copies from second-level store if not present in first`() {
    whenever(first.read(NICE_KEY)) doThrow FileDoesntExistException("Oh")
    whenever(second.read(NICE_KEY)) doReturn NICE_DATA

    val ret = store.read(NICE_KEY)

    assertArrayEquals(NICE_DATA, ret)
    verify(first).write(NICE_KEY, NICE_DATA)
  }

  @Test
  fun `propagates exception if not found in either`() {
    whenever(first.read(NICE_KEY)) doThrow FileDoesntExistException("Oh")
    whenever(second.read(NICE_KEY)) doThrow FileDoesntExistException("No")

    assertThrows<FileDoesntExistException> {
      store.read(NICE_KEY)
    }
  }

  companion object {
    private const val NICE_KEY = "foo"
    private val NICE_DATA = byteArrayOf(1, 2, 3, 4)
  }
}
