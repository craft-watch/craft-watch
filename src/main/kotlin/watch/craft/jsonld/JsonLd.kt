package watch.craft.jsonld

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES
import com.fasterxml.jackson.databind.deser.ContextualDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.jsoup.nodes.Document
import watch.craft.MalformedInputException
import watch.craft.jsonld.Thing.Offer
import watch.craft.jsonld.Thing.Product
import watch.craft.dsl.selectMultipleFrom
import java.net.URI

inline fun <reified T : Any> Document.jsonLdFrom(): T {
  val mapper = jsonLdMapper()
  try {
    val things = selectMultipleFrom("script[type=application/ld+json]")
      .flatMap {
        val thing = mapper.readValue<Thing?>(it.data())
        if (thing == null) {
          try {
            mapper.readValue<watch.craft.jsonld.Document>(it.data()).graph
          } catch (e: JsonProcessingException) {
            emptyList<Thing>()
          }
        } else {
          listOf(thing)
        }
      }
    return things.filterIsInstance<T>().single()
  } catch (e: NoSuchElementException) {
    throw MalformedInputException("Couldn't find JSON-LD data for that type", e)
  }
}

fun jsonLdMapper() = jacksonObjectMapper()
  .disable(FAIL_ON_UNKNOWN_PROPERTIES)
  .registerModule(
    SimpleModule().addDeserializer(List::class.java, JsonLdListDeserializer())
  )!!

private class JsonLdListDeserializer(
  private val valueType: JavaType? = null
) : JsonDeserializer<List<*>>(), ContextualDeserializer {
  override fun createContextual(ctxt: DeserializationContext, property: BeanProperty) =
    JsonLdListDeserializer(property.type.containedType(0))

  override fun deserialize(p: JsonParser, ctxt: DeserializationContext): List<Any> {
    val mapper = (p.codec as ObjectMapper)
    val root = p.codec.readTree<JsonNode>(p)
    val nodes = if (root is ArrayNode) root else listOf(root)
    return nodes.map { mapper.convertValue<Any>(it, valueType) }
  }
}

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "@type", defaultImpl = Void::class)
@JsonSubTypes(
  JsonSubTypes.Type(Product::class, name = "Product"),
  JsonSubTypes.Type(Offer::class, name = "Offer")
)
sealed class Thing {
  data class Product(
    val description: String,
    val image: List<URI>,
    val offers: List<Offer>
  ) : Thing()

  data class Offer(
    val sku: String? = null,
    val price: Double,
    val availability: String
  ) : Thing()
}

data class Document(
  @JsonProperty("@graph")
  val graph: List<Thing>
)

