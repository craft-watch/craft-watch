package watch.craft.schemas

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import watch.craft.schemas.Thing.Offer
import watch.craft.schemas.Thing.Product

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
