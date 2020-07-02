package watch.craft.analysis

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import watch.craft.Item

class CategoriserTest {

  @Test
  fun `case insensitive`() {
    val categoriser = Categoriser(mapOf("foo" to listOf("foo")))

    assertEquals(setOf("foo"), categoriser.categorise(item("foo")))
    assertEquals(setOf("foo"), categoriser.categorise(item("Foo")))
  }

  @Test
  fun `matches in all components`() {
    val categoriser = Categoriser(mapOf("foo" to listOf("foo")))

    assertEquals(setOf("foo"), categoriser.categorise(item(name = "foo")))
    assertEquals(setOf("foo"), categoriser.categorise(item(name = "abc", summary = "foo")))
    assertEquals(setOf("foo"), categoriser.categorise(item(name = "abc", desc = "foo")))
  }

  @Test
  fun `matches any synonym back to category`() {
    val categoriser = Categoriser(mapOf("foo" to listOf("foo", "bar")))

    assertEquals(setOf("foo"), categoriser.categorise(item(name = "foo")))
    assertEquals(setOf("foo"), categoriser.categorise(item(name = "bar")))
  }

  @Test
  fun `matches multiple categories`() {
    val categoriser = Categoriser(mapOf("foo" to listOf("foo"), "bar" to listOf("bar")))

    assertEquals(setOf("foo"), categoriser.categorise(item(name = "foo")))
    assertEquals(setOf("bar"), categoriser.categorise(item(name = "bar")))
    assertEquals(setOf("foo", "bar"), categoriser.categorise(item(name = "foo bar")))
  }

  @Test
  fun `longest match wins`() {
    val categoriser = Categoriser(mapOf("foo" to listOf("foo"), "bar" to listOf("foo bar")))

    assertEquals(setOf("foo"), categoriser.categorise(item(name = "foo")))
    assertEquals(setOf("bar"), categoriser.categorise(item(name = "foo bar")))
  }

  @Test
  fun `only matches against complete words`() {
    val categoriser = Categoriser(mapOf("foo" to listOf("foo bar")))
    fun assertMatch(text: String) = assertEquals(setOf("foo"), categoriser.categorise(item(text)))
    fun assertNoMatch(text: String) = assertEquals(emptySet<String>(), categoriser.categorise(item(text)))

    assertMatch("foo bar")

    assertMatch("abc foo bar")
    assertMatch("foo bar def")
    assertMatch("abc foo bar def")

    assertMatch(".foo bar")
    assertMatch("foo bar.")
    assertMatch(".foo bar.")

    assertNoMatch("abcfoo bar")
    assertNoMatch("foo bardef")
  }

  private fun item(name: String, summary: String? = null, desc: String? = null) = mock<Item> {
    on { this.name } doReturn name
    on { this.summary } doReturn summary
    on { this.desc } doReturn desc
  }
}
