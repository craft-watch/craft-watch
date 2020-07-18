package watch.craft

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.stub
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import watch.craft.ResultsManager.MinimalInventory
import watch.craft.ResultsManager.MinimalItem
import watch.craft.storage.ObjectStore
import java.time.Instant

class ResultsManagerTest {
  private val store = mock<ObjectStore>()
  private val manager = ResultsManager(mock { on { results } doReturn store })

  @Test
  fun `handles v1 inventory`() {
    val json = """
      {
        "version": 1,
        "items": [
          {
            "breweryId": "pollys-brew",
            "name": "Foo"
          }
        ]
      }
    """.trimIndent()

    store.stub {
      onBlocking { read(any()) } doReturn json.toByteArray()
    }

    assertEquals(
      MinimalInventory(
        version = 1,
        items = listOf(MinimalItem(breweryId = "pollys-brew", name = "Foo"))
      ),
      runBlocking { manager.readMinimalHistoricalResult(Instant.now()) }
    )
  }

  @Test
  fun `converts v0 inventory`() {
    val json = """
      {
        "items": [
          {
            "brewery": "Polly's Brew",
            "name": "Foo"
          }
        ]
      }
    """.trimIndent()

    store.stub {
      onBlocking { read(any()) } doReturn json.toByteArray()
    }

    assertEquals(
      MinimalInventory(
        items = listOf(MinimalItem(breweryId = "pollys-brew", name = "Foo"))
      ),
      runBlocking { manager.readMinimalHistoricalResult(Instant.now()) }
    )
  }

  @Test
  fun `gracefully rejects unknown v0 breweries`() {
    val json = """
      {
        "items": [
          {
            "brewery": "Nonsense",
            "name": "Foo"
          }
        ]
      }
    """.trimIndent()

    store.stub {
      onBlocking { read(any()) } doReturn json.toByteArray()
    }

    assertEquals(
      MinimalInventory(
        items = emptyList()
      ),
      runBlocking { manager.readMinimalHistoricalResult(Instant.now()) }
    )
  }
}
