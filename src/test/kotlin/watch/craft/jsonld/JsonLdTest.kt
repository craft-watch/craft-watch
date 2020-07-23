package watch.craft.jsonld

import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import watch.craft.jsonld.Thing.Offer
import watch.craft.jsonld.Thing.Product

class JsonLdTest {
  private val mapper = jsonLdMapper()

  @Nested
  inner class Lists {
    @Test
    fun `parses non-list singleton value`() {
      val json = """
        {
          "@type": "Product",
          "name": "A",
          "description": "Hello",
          "image": "https://example.invalid",
          "offers": {
            "@type": "Offer",
            "price": 4.23,
            "availability": "InStock"
          }
        }
      """.trimIndent()

      assertEquals(
        listOf(
          Offer(price = 4.23, availability = "InStock")
        ),
        mapper.readValue<Product>(json).offers
      )
    }

    @Test
    fun `parses list value`() {
      val json = """
        {
          "@type": "Product",
          "name": "A",
          "description": "Hello",
          "image": "https://example.invalid",
          "offers": [
            {
              "@type": "Offer",
              "price": 4.23,
              "availability": "InStock"
            },
            {
              "@type": "Offer",
              "price": 8.46,
              "availability": "InStock"
            }
          ]
        }
      """.trimIndent()

      assertEquals(
        listOf(
          Offer(price = 4.23, availability = "InStock"),
          Offer(price = 8.46, availability = "InStock")
        ),
        mapper.readValue<Product>(json).offers
      )
    }

    @Test
    fun `skips unrecognised sub-object values`() {
      val json = """
        {
          "@type": "Product",
          "name": "A",
          "description": "Hello",
          "image": "https://example.invalid",
          "offers": {
            "@type": "AggregateOffer"
          }
        }
      """.trimIndent()

      assertEquals(
        emptyList<Offer>(),
        mapper.readValue<Product>(json).offers
      )
    }
  }

  @Test
  fun `supports deep nesting`() {
    val json = """
      {
        "@type": "Product",
        "name": "A",
        "image": "https://example.invalid",
        "description": "Nice beer.",
        "offers": {
          "@type": "AggregateOffer"
        },
        "model": [
          {
            "@type": "ProductModel",
            "name": "B",
            "image": "https://example.invalid",
            "offers": {
              "@type": "Offer",
              "price": 4.23,
              "availability": "InStock"
            }
          },
          {
            "@type": "ProductModel",
            "name": "C",
            "image": "https://example.invalid",
            "offers": {
              "@type": "Offer",
              "price": 8.46,
              "availability": "InStock"
            }
          }
        ]
      }
    """.trimIndent()

    assertEquals(
      listOf(
        Offer(price = 4.23, availability = "InStock"),
        Offer(price = 8.46, availability = "InStock")
      ),
      mapper.readValue<Product>(json).model.flatMap { it.offers }
    )
  }
}
