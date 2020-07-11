package watch.craft.jsonld

import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class JsonLdTest {
  private val mapper = jsonLdMapper()

  private data class Foo(val bars: List<Bar>)
  private data class Bar(val a: Int)

  @Test
  fun `parses list field with singleton value`() {
    val json = """
      {
        "bars": {
          "a": 123
        }
      }
    """.trimIndent()

    assertEquals(
      Foo(
        bars = listOf(
          Bar(a = 123)
        )
      ),
      mapper.readValue<Foo>(json)
    )
  }

  @Test
  fun `parses list field with list value`() {
    val json = """
      {
        "bars": [
          { "a": 123 },
          { "a": 456 }
        ]
      }
    """.trimIndent()

    assertEquals(
      Foo(
        bars = listOf(
          Bar(a = 123),
          Bar(a = 456)
        )
      ),
      mapper.readValue<Foo>(json)
    )
  }
}
