package watch.craft.executor

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import watch.craft.*
import java.net.URI
import java.time.Duration
import java.time.Instant

class NewalyserTest {
  private val now = Instant.parse("2007-12-03T10:15:30.00Z")

  @Nested
  inner class Items {
    @Test
    fun `doesn't mark as new if match within window`() {
      val newalyser = createNewalyser(
        mapOf(
          11 to listOf(INTERESTING_ITEM)
        )
      )

      assertFalse(newalyser.enrich(INTERESTING_ITEM).new)
    }

    @Test
    fun `marks as new if no match within window`() {
      val newalyser = createNewalyser(
        mapOf(
          11 to listOf(SAME_BREWERY_ITEM)
        )
      )

      assertTrue(newalyser.enrich(INTERESTING_ITEM).new)
    }

    @Test
    fun `marks as new if exact match before window`() {
      val newalyser = createNewalyser(
        mapOf(
          11 to listOf(SAME_BREWERY_ITEM),
          2 to listOf(INTERESTING_ITEM)
        )
      )

      assertTrue(newalyser.enrich(INTERESTING_ITEM).new)
    }

    @Test
    fun `marks as new if exact match after window`() {
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
        on {
          readMinimalHistoricalResult(now - Duration.ofDays(daysAgo.toLong()))
        } doReturn MinimalInventory(items = items.map { MinimalItem(brewery = it.brewery, name = it.name) })
      }
    }
    return Newalyser(manager, now)
  }

  companion object {
    private val BREWERY = Brewery(
      shortName = "abc",
      name = "Abc Brewing",
      location = "Space",
      websiteUrl = URI("https://example.invalid")
    )
    private val INTERESTING_ITEM = PROTOTYPE_ITEM.copy(brewery = "abc", name = "def")
    private val SAME_BREWERY_ITEM = PROTOTYPE_ITEM.copy(brewery = "abc", name = "ghi")
    private val DIFFERENT_BREWERY_ITEM = PROTOTYPE_ITEM.copy(brewery = "xyz", name = "ghi")
  }
}
