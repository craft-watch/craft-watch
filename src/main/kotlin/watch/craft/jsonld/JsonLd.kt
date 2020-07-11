package watch.craft.jsonld

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import watch.craft.jsonld.Thing.Offer
import watch.craft.jsonld.Thing.Product

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "@type", defaultImpl = Void::class)
@JsonSubTypes(
  JsonSubTypes.Type(Product::class, name = "Product"),
  JsonSubTypes.Type(Offer::class, name = "Offer")
)
sealed class Thing {
  data class Product(
    val offers: List<Offer>
  ) : Thing()

  data class Offer(
    val availability: String
  ) : Thing()
}
