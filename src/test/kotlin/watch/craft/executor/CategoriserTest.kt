package watch.craft.executor

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import watch.craft.Item
import watch.craft.PROTOTYPE_ITEM
import watch.craft.executor.Categoriser.Component.DESC
import watch.craft.executor.Categoriser.Component.NAME
import watch.craft.executor.Categoriser.Synonym

class CategoriserTest {
  @Test
  fun `case insensitive`() {
    val categoriser = Categoriser(mapOf(
            "foo" to listOf(Synonym("foo"))
    ))

    assertEquals(listOf("foo"), categoriser.categorise(item("foo")))
    assertEquals(listOf("foo"), categoriser.categorise(item("Foo")))
  }

  @Test
  fun `matches in all components`() {
    val categoriser = Categoriser(mapOf(
            "foo" to listOf(Synonym("foo"))
    ))

    assertEquals(listOf("foo"), categoriser.categorise(item(name = "foo")))
    assertEquals(listOf("foo"), categoriser.categorise(item(name = "abc", summary = "foo")))
    assertEquals(listOf("foo"), categoriser.categorise(item(name = "abc", desc = "foo")))
  }

  @Test
  fun `matches in specified components`() {
    val categoriser = Categoriser(mapOf(
            "foo" to listOf(Synonym("foo", setOf(NAME, DESC)))
    ))

    assertEquals(listOf("foo"), categoriser.categorise(item(name = "foo")))
    assertEquals(emptyList<String>(), categoriser.categorise(item(name = "abc", summary = "foo")))
    assertEquals(listOf("foo"), categoriser.categorise(item(name = "abc", desc = "foo")))
  }

  @Test
  fun `matches any synonym back to category`() {
    val categoriser = Categoriser(mapOf(
            "foo" to listOf(
                    Synonym("foo"),
                    Synonym("bar")
            )
    ))

    assertEquals(listOf("foo"), categoriser.categorise(item(name = "foo")))
    assertEquals(listOf("foo"), categoriser.categorise(item(name = "bar")))
  }

  @Test
  fun `matches multiple categories`() {
    val categoriser = Categoriser(mapOf(
            "foo" to listOf(Synonym("foo")),
            "bar" to listOf(Synonym("bar"))
    ))

    assertEquals(listOf("foo"), categoriser.categorise(item(name = "foo")))
    assertEquals(listOf("bar"), categoriser.categorise(item(name = "bar")))
    assertEquals(listOf("foo", "bar"), categoriser.categorise(item(name = "foo bar")))
  }

  @Test
  fun `longest match wins`() {
    val categoriser = Categoriser(mapOf(
            "foo" to listOf(Synonym("foo")),
            "bar" to listOf(Synonym("foo bar"))
    ))

    assertEquals(listOf("foo"), categoriser.categorise(item(name = "foo")))
    assertEquals(listOf("bar"), categoriser.categorise(item(name = "foo bar")))
  }

  @Test
  fun `only matches against complete words`() {
    val categoriser = Categoriser(mapOf(
            "foo" to listOf(Synonym("foo bar"))
    ))

    fun assertMatch(text: String) = assertEquals(listOf("foo"), categoriser.categorise(item(text)))
    fun assertNoMatch(text: String) = assertEquals(emptyList<String>(), categoriser.categorise(item(text)))

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

  private fun Categoriser.categorise(item: Item) = enrich(item).categories

  private fun item(name: String, summary: String? = null, desc: String? = null) = PROTOTYPE_ITEM.copy(
    name = name,
    summary = summary,
    desc = desc
  )
}
