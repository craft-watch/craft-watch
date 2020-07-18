package watch.craft.executor

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import watch.craft.Brewery
import watch.craft.Item
import watch.craft.PROTOTYPE_ITEM
import watch.craft.ResultsManager
import watch.craft.ResultsManager.MinimalInventory
import watch.craft.ResultsManager.MinimalItem
import java.net.URI
import java.time.Duration
import java.time.Instant

class NewalyserTest {
  private val now = Instant.parse("2007-12-03T10:15:30.00Z")

  @Nested
  inner class Items {
    @Test
    fun `doesn't mark as new if first occurrence of brewery`() {
      val newalyser = createNewalyser(
        emptyMap()
      )

      assertFalse(newalyser.enrich(INTERESTING_ITEM).new)
    }

    @Test
    fun `marks as new if earlier occurrence of brewery`() {
      val newalyser = createNewalyser(
        mapOf(
          11 to listOf(SAME_BREWERY_ITEM)
        )
      )

      assertTrue(newalyser.enrich(INTERESTING_ITEM).new)
    }

    @Test
    fun `doesn't mark as new if earlier occurrence of item in window`() {
      val newalyser = createNewalyser(
        mapOf(
          11 to listOf(INTERESTING_ITEM)
        )
      )

      assertFalse(newalyser.enrich(INTERESTING_ITEM).new)
    }

    @Test
    fun `marks as new if if earlier occurrence of item before window`() {
      val newalyser = createNewalyser(
        mapOf(
          11 to listOf(SAME_BREWERY_ITEM),
          2 to listOf(INTERESTING_ITEM)
        )
      )

      assertTrue(newalyser.enrich(INTERESTING_ITEM).new)
    }

    @Test
    fun `marks as new if if earlier occurrence of item after window`() {
      val newalyser = createNewalyser(
        mapOf(
          11 to listOf(SAME_BREWERY_ITEM),
          18 to listOf(INTERESTING_ITEM)
        )
      )

      assertTrue(newalyser.enrich(INTERESTING_ITEM).new)
    }

    @Test
    fun `matches case-insensitively`() {
      val newalyser = createNewalyser(
        mapOf(
          11 to listOf(INTERESTING_ITEM.run { copy(name = name.toUpperCase()) })
        )
      )

      assertFalse(newalyser.enrich(INTERESTING_ITEM).new)
    }
  }

  @Nested
  inner class Brewery {
    @Test
    fun `doesn't mark as new if brewery found within window`() {
      val newalyser = createNewalyser(
        mapOf(
          11 to listOf(INTERESTING_ITEM)
        )
      )

      assertFalse(newalyser.enrich(BREWERY).new)
    }

    @Test
    fun `marks as new if brewery not found within window`() {
      val newalyser = createNewalyser(
        mapOf(
          11 to listOf(DIFFERENT_BREWERY_ITEM)
        )
      )

      assertTrue(newalyser.enrich(BREWERY).new)
    }

    @Test
    fun `marks as new if brewery only found before window`() {
      val newalyser = createNewalyser(
        mapOf(
          2 to listOf(INTERESTING_ITEM)
        )
      )

      assertTrue(newalyser.enrich(BREWERY).new)
    }

    @Test
    fun `marks as new if brewery only found after window`() {
      val newalyser = createNewalyser(
        mapOf(
          18 to listOf(INTERESTING_ITEM)
        )
      )

      assertTrue(newalyser.enrich(BREWERY).new)
    }
  }

  private fun createNewalyser(results: Map<Int, List<Item>>): Newalyser {
    val manager = mock<ResultsManager> {
      on { listHistoricalResults() } doReturn results.keys.map { daysAgo -> now - Duration.ofDays(daysAgo.toLong()) }
      results.forEach { (daysAgo, items) ->
        onBlocking {
          readMinimalHistoricalResult(now - Duration.ofDays(daysAgo.toLong()))
        } doReturn MinimalInventory(items = items.map { MinimalItem(breweryId = it.breweryId, name = it.name) })
      }
    }
    return Newalyser(manager, now)
  }

  companion object {
    private val BREWERY = Brewery(
      id = "abc",
      shortName = "ABC",
      name = "Abc Brewing",
      location = "Space",
      websiteUrl = URI("https://example.invalid")
    )
    private val INTERESTING_ITEM = PROTOTYPE_ITEM.copy(breweryId = "abc", name = "def")
    private val SAME_BREWERY_ITEM = PROTOTYPE_ITEM.copy(breweryId = "abc", name = "ghi")
    private val DIFFERENT_BREWERY_ITEM = PROTOTYPE_ITEM.copy(breweryId = "xyz", name = "ghi")
  }
}
