package watch.craft.storage

import com.nhaarman.mockitokotlin2.*
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows

class ObjectStoresTest {
  private val front = mock<ObjectStore>()
  private val back = mock<ObjectStore>()
  private val store = back.frontedBy(front)

  @Test
  fun `writes to both stores in appropriate order`() {
    write()

    inOrder(front, back) {
      verifyBlocking(back) { write(NICE_KEY, NICE_DATA) }
      verifyBlocking(front) { write(NICE_KEY, NICE_DATA) }
    }
  }

  @Test
  fun `still writes to first store if already present in second`() {
    back.stub {
      onBlocking { write(any(), any()) } doThrow FileExistsException("oh")
    }

    write()

    verifyBlocking(front) { write(NICE_KEY, NICE_DATA) }
  }

  @Test
  fun `throws if already present in both`() {
    front.stub {
      onBlocking { write(any(), any()) } doThrow FileExistsException("oh")
    }
    back.stub {
      onBlocking { write(any(), any()) } doThrow FileExistsException("oh")
    }

    assertThrows<FileExistsException> { write() }
  }

  @Test
  fun `reads from first-level store and not from second if present in first`() {
    front.stub {
      onBlocking { read(NICE_KEY) } doReturn NICE_DATA
    }

    val ret = read()

    assertArrayEquals(NICE_DATA, ret)
    verifyBlocking(back, never()) { read(any()) }
  }

  @Test
  fun `reads and copies from second-level store if not present in first`() {
    front.stub {
      onBlocking { read(NICE_KEY) } doThrow FileDoesntExistException("oh")
    }
    back.stub {
      onBlocking { read(NICE_KEY) } doReturn NICE_DATA
    }

    val ret = read()

    assertArrayEquals(NICE_DATA, ret)
    verifyBlocking(front) { write(NICE_KEY, NICE_DATA) }
  }

  @Test
  fun `propagates exception if not found in either`() {
    front.stub {
      onBlocking { read(NICE_KEY) } doThrow FileDoesntExistException("oh")
    }
    back.stub {
      onBlocking { read(NICE_KEY) } doThrow FileDoesntExistException("oh")
    }

    assertThrows<FileDoesntExistException> { read() }
  }

  @Test
  fun `don't throw if data unexpectedly appears in first-level store when we try to copy it`() {
    front.stub {
      onBlocking { read(NICE_KEY) } doThrow FileDoesntExistException("oh")
    }
    front.stub {
      onBlocking { write(any(), any()) } doThrow FileExistsException("oh")
    }
    back.stub {
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
