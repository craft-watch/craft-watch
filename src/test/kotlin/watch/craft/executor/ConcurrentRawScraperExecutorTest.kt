package watch.craft.executor

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import watch.craft.Scraper.ScrapedItem
import watch.craft.executor.ScraperAdapter.Result

class ConcurrentRawScraperExecutorTest {
  private val exec = ConcurrentRawScraperExecutor()

  private val itemA = mock<ScrapedItem>()
  private val itemB = mock<ScrapedItem>()

  // TODO - test structured concurrency on error

  @Test
  fun `executes single item`() {
    assertEquals(
      setOf(itemA),
      execute(
        mockAdapter(listOf(
          mapOf("A" to itemA)
        ))
      )
    )
  }

  @Test
  fun `executes multiple items from single root`() {
    assertEquals(
      setOf(itemA, itemB),
      execute(
        mockAdapter(listOf(
          mapOf("A" to itemA, "B" to itemB)
        ))
      )
    )
  }

  @Test
  fun `executes items from multiples roots`() {
    assertEquals(
      setOf(itemA, itemB),
      execute(
        mockAdapter(listOf(
          mapOf("A" to itemA),
          mapOf("B" to itemB)
        ))
      )
    )
  }

  @Test
  fun `executes items from multiples scrapers`() {
    assertEquals(
      setOf(itemA, itemB),
      execute(
        mockAdapter(listOf(
          mapOf("A" to itemA)
        )),
        mockAdapter(listOf(
          mapOf("B" to itemB)
        ))
      )
    )
  }

  private fun execute(vararg adapters: ScraperAdapter) =
    exec.execute(adapters.toList()).map { it.item }.toSet()

  private fun mockAdapter(mappings: List<Map<String, ScrapedItem>>) = mock<ScraperAdapter> {
    on { indexTasks } doReturn mappings.map { root ->
      {
        root.map { (name, item) ->
          { Result(breweryName = name, entry = mock(), item = item) }
        }
      }
    }
  }
}
