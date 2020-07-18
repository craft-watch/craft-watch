package watch.craft.network

import com.nhaarman.mockitokotlin2.*
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import watch.craft.storage.FileDoesntExistException
import watch.craft.storage.FileExistsException
import watch.craft.storage.SubObjectStore
import watch.craft.utils.sha1
import java.net.URI

// TODO - test suffix
class CachingRetrieverTest {
  private val store = mock<SubObjectStore>()
  private val delegate = mock<Retriever>()
  private val retriever = CachingRetriever(store, delegate)

  @Test
  fun `get from store and not from network if already in store`() {
    store.stub {
      onBlocking { read(NICE_KEY) } doReturn NICE_DATA
    }

    val ret = runBlocking { retriever.retrieve(NICE_URL) {} }

    assertArrayEquals(NICE_DATA, ret)
    verifyBlocking(delegate, never()) { retrieve(any(), any(), any()) }
  }

  @Test
  fun `get from network and write to store if not already in store`() {
    store.stub {
      onBlocking { read(NICE_KEY) } doThrow FileDoesntExistException("oh")
    }
    delegate.stub {
      onBlocking { retrieve(eq(NICE_URL), anyOrNull(), any()) } doReturn NICE_DATA
    }

    val ret = runBlocking { retriever.retrieve(NICE_URL) {} }

    assertArrayEquals(NICE_DATA, ret)
    verifyBlocking(store) { write(NICE_KEY, NICE_DATA) }
  }

  @Test
  fun `don't throw if data unexpectedly appears in store when we try to write it`() {
    store.stub {
      onBlocking { read(NICE_KEY) } doThrow FileDoesntExistException("oh")
      onBlocking { write(any(), any()) } doThrow FileExistsException("no")
    }
    delegate.stub {
      onBlocking { retrieve(eq(NICE_URL), anyOrNull(), any()) } doReturn NICE_DATA
    }

    assertDoesNotThrow {
      runBlocking { retriever.retrieve(NICE_URL) {} }
    }
  }

  companion object {
    private val NICE_URL = URI("https://example.invalid")
    private val NICE_KEY = NICE_URL.toString().sha1()
    private val NICE_DATA = byteArrayOf(1, 2, 3, 4)
  }
}
