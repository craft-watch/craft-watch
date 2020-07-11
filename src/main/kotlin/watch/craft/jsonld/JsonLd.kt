package watch.craft.jsonld

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES
import com.fasterxml.jackson.databind.deser.ContextualDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import watch.craft.jsonld.Thing.Offer
import watch.craft.jsonld.Thing.Product
import java.net.URI

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
    val price: Double,
    val availability: String
  ) : Thing()
}
