package watch.craft.jsonld

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.core.json.JsonReadFeature.ALLOW_UNESCAPED_CONTROL_CHARS
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES
import com.fasterxml.jackson.databind.deser.ContextualDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.jsoup.nodes.Document
import watch.craft.MalformedInputException
import watch.craft.dsl.selectMultipleFrom
import watch.craft.jsonld.Thing.*

inline fun <reified T : Any> Document.jsonLdFrom(): List<T> {
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
    return things.filterIsInstance<T>()
  } catch (e: NoSuchElementException) {
    throw MalformedInputException("Couldn't find JSON-LD data for that type", e)
  }
}

fun jsonLdMapper() = jacksonObjectMapper()
  .disable(FAIL_ON_UNKNOWN_PROPERTIES)
  .enable(ALLOW_UNESCAPED_CONTROL_CHARS.mappedFeature())
  .registerModule(
    SimpleModule().addDeserializer(List::class.java, ListDeserializer())
  )!!

private class ListDeserializer(
  private val valueType: JavaType? = null
) : JsonDeserializer<List<*>>(), ContextualDeserializer {
  override fun createContextual(ctxt: DeserializationContext, property: BeanProperty) =
    ListDeserializer(property.type.containedType(0))

  override fun deserialize(p: JsonParser, ctxt: DeserializationContext): List<Any> {
    val mapper = (p.codec as ObjectMapper)
    val root = p.codec.readTree<JsonNode>(p)
    val nodes = if (root is ArrayNode) root else listOf(root)
    return nodes.mapNotNull { mapper.convertValue<Any>(it, valueType) }
  }
}

@JsonTypeInfo(
  use = JsonTypeInfo.Id.NAME,
  property = "@type",
  defaultImpl = Void::class
)
@JsonSubTypes(
  JsonSubTypes.Type(Product::class, name = "Product"),
  JsonSubTypes.Type(ProductModel::class, name = "ProductModel"),
  JsonSubTypes.Type(Offer::class, name = "Offer"),
  JsonSubTypes.Type(PropertyValue::class, name = "PropertyValue")
)
sealed class Thing {
  data class Product(
    val name: String,
    val description: String? = null,
    val offers: List<Offer> = emptyList(),
    val model: List<ProductModel> = emptyList()
  ) : Thing()

  data class ProductModel(
    val name: String,
    val additionalProperty: List<PropertyValue> = emptyList(),
    val offers: List<Offer>
  ) : Thing()

  data class Offer(
    val name: String? = null,
    val sku: String? = null,
    val price: Double,
    val availability: String,
    val itemOffered: Product? = null
  ) : Thing()

  data class PropertyValue(
    val name: String,
    val value: String
  ) : Thing()
}

data class Document(
  @JsonProperty("@graph")
  val graph: List<Thing>
)

