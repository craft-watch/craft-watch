package watch.craft.network

import com.nhaarman.mockitokotlin2.*
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import watch.craft.storage.FileDoesntExistException
import watch.craft.storage.FileExistsException
import watch.craft.storage.SubObjectStore
import watch.craft.utils.sha1
import java.net.URI

class CachingGetterTest {
  private val store = mock<SubObjectStore>()
  private val networkGet = mock<(URI) -> ByteArray>()
  private val getter = CachingGetter(store, networkGet)

  @Test
  fun `get from store and not from network if already in store`() {
    whenever(store.read(NICE_KEY)) doReturn NICE_DATA

    val ret = getter.request(NICE_URL)

    assertArrayEquals(NICE_DATA, ret)
    verify(networkGet, never())(any())
  }

  @Test
  fun `get from network and write to store if not already in store`() {
    whenever(store.read(NICE_KEY)) doThrow FileDoesntExistException("oh")
    whenever(networkGet(NICE_URL)) doReturn NICE_DATA

    val ret = getter.request(NICE_URL)

    assertArrayEquals(NICE_DATA, ret)
    verify(store).write(NICE_KEY, NICE_DATA)
  }

  @Test
  fun `don't throw if data unexpectedly appears in store when we try to write it`() {
    whenever(store.read(NICE_KEY)) doThrow FileDoesntExistException("oh")
    whenever(store.write(any(), any())) doThrow FileExistsException("no")
    whenever(networkGet(NICE_URL)) doReturn NICE_DATA

    assertDoesNotThrow {
      getter.request(NICE_URL)
    }
  }

  companion object {
    private val NICE_URL = URI("https://example.invalid")
    private val NICE_KEY = NICE_URL.toString().sha1()
    private val NICE_DATA = byteArrayOf(1, 2, 3, 4)
  }
}
