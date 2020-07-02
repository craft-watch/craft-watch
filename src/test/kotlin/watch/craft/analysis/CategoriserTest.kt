package watch.craft.analysis

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import watch.craft.Item

class CategoriserTest {


  @Test
  fun `only matches against complete words`() {
    val categoriser = Categoriser(mapOf("foo" to listOf("foo bar")))

    assertEquals(
      listOf("foo"),
      categoriser.categorise(item("foo bar"))
    )

    assertEquals(
      listOf("foo"),
      categoriser.categorise(item("abc foo bar"))
    )

    assertEquals(
      listOf("foo"),
      categoriser.categorise(item("foo bar def"))
    )

    assertEquals(
      listOf("foo"),
      categoriser.categorise(item("abc foo bar def"))
    )

    assertEquals(
      emptyList<String>(),
      categoriser.categorise(item("abcfoo bar"))
    )

    assertEquals(
      emptyList<String>(),
      categoriser.categorise(item("foo bardef"))
    )
  }


  private fun item(name: String, summary: String? = null, desc: String? = null) = mock<Item> {
    on { this.name } doReturn name
    on { this.summary } doReturn summary
    on { this.desc } doReturn desc
  }
}
