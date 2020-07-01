package watch.craft.storage

import com.nhaarman.mockitokotlin2.*
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import watch.craft.sha1

class BlobManagerTest {
  private val store = mock<ObjectStore>()
  private val manager = BlobManager(store)

  @Test
  fun `reads data if present`() {
    whenever(store.read(NICE_KEY)) doReturn NICE_DATA

    assertArrayEquals(NICE_DATA, manager.read(NICE_KEY))
  }

  @Test
  fun `propagates exception if not present`() {
    whenever(store.read(NICE_KEY)) doThrow FileDoesntExistException("Uh oh")

    assertThrows<FileDoesntExistException> {
      manager.read(NICE_KEY)
    }
  }

  @Test
  fun `writes data and returns key if not already present`() {
    val key = manager.write(NICE_DATA)

    assertEquals(NICE_KEY, key)
    verify(store).write(NICE_KEY, NICE_DATA)
  }

  @Test
  fun `no worries if data already present`() {
    whenever(store.write(any(), any())) doThrow FileExistsException("Uh oh")

    val key = assertDoesNotThrow {
      manager.write(NICE_DATA)
    }

    assertEquals(NICE_KEY, key)
  }

  companion object {
    private val NICE_DATA = byteArrayOf(1, 2, 3, 4)
    private val NICE_KEY = NICE_DATA.sha1()
  }
}
