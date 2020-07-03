package watch.craft.enrichers

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import watch.craft.*
import java.time.Duration
import java.time.Instant

class NewalyserTest {
  private val now = Instant.parse("2007-12-03T10:15:30.00Z")

  @Test
  fun `doesn't mark as new if exact match in historical window`() {
    val newalyser = createNewalyser(mapOf(
      11 to listOf(INTERESTING_ITEM)
    ))

    val result = newalyser.enrich(INTERESTING_ITEM)

    assertFalse(result.newFromBrewer)
    assertFalse(result.newToUs)
  }

  @Test
  fun `marks as new if no beer match in historical window`() {
    val newalyser = createNewalyser(mapOf(
      11 to listOf(SAME_BREWERY_ITEM)
    ))

    val result = newalyser.enrich(INTERESTING_ITEM)

    assertTrue(result.newFromBrewer)
    assertTrue(result.newToUs)
  }

  @Test
  fun `only mark as new from brewer if no beer match in historical window but brewery is also new`() {
    val newalyser = createNewalyser(mapOf(
      11 to listOf(DIFFERENT_BREWERY_ITEM)
    ))

    val result = newalyser.enrich(INTERESTING_ITEM)

    assertFalse(result.newFromBrewer)
    assertTrue(result.newToUs)
  }

  @Test
  fun `marks as new if exact match before historical window`() {
    val newalyser = createNewalyser(mapOf(
      11 to listOf(SAME_BREWERY_ITEM),
      2 to listOf(INTERESTING_ITEM)
    ))

    val result = newalyser.enrich(INTERESTING_ITEM)

    assertTrue(result.newFromBrewer)
    assertTrue(result.newToUs)
  }

  @Test
  fun `marks as new if exact match after historical window`() {
    val newalyser = createNewalyser(mapOf(
      11 to listOf(SAME_BREWERY_ITEM),
      18 to listOf(INTERESTING_ITEM)
    ))

    val result = newalyser.enrich(INTERESTING_ITEM)

    assertTrue(result.newFromBrewer)
    assertTrue(result.newToUs)
  }

  @Test
  fun `matches beers case-insensitively`() {
    val newalyser = createNewalyser(mapOf(
      11 to listOf(INTERESTING_ITEM.run { copy(name = name.toUpperCase()) })
    ))

    val result = newalyser.enrich(INTERESTING_ITEM)

    assertFalse(result.newFromBrewer)
    assertFalse(result.newToUs)
  }

  private fun createNewalyser(results: Map<Int, List<Item>>): Newalyser {
    val manager = mock<ResultsManager> {
      on { listHistoricalResults() } doReturn results.keys.map { daysAgo -> now - Duration.ofDays(daysAgo.toLong()) }
      results.forEach { (daysAgo, items) ->
        on {
          readMinimalHistoricalResult(now - Duration.ofDays(daysAgo.toLong()))
        } doReturn MinimalInventory(items = items.map { MinimalItem(brewery = it.brewery, name = it.name) } )
      }
    }
    return Newalyser(manager, now)
  }

  companion object {
    private val INTERESTING_ITEM = PROTOTYPE_ITEM.copy(brewery = "abc", name = "def")
    private val SAME_BREWERY_ITEM = PROTOTYPE_ITEM.copy(brewery = "abc", name = "ghi")
    private val DIFFERENT_BREWERY_ITEM = PROTOTYPE_ITEM.copy(brewery = "xyz", name = "ghi")
  }
}
