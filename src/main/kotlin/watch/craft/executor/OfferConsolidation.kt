package watch.craft.executor

import mu.KotlinLogging
import watch.craft.DEFAULT_SIZE_ML
import watch.craft.Inventory
import watch.craft.Item
import watch.craft.Offer
import kotlin.math.round

private val logger = KotlinLogging.logger {}

fun Inventory.consolidateOffers() =
  copy(items = items.groupBy { ItemGroupFields(it.brewery, it.name.toLowerCase()) }
    .map { (key, group) ->
      if (group.size > 1) {
        logger.info("[${key.brewery}] Merging ${group.size} item(s) for [${key.name}]")
      }

      val offers = group.mergeAndPrioritiseOffers()

      val headlineItem = offers.first().item

      headlineItem.copy(offers = offers.map { it.offer })
    }
  )

// TODO - need a stable notion of "first" - will need to sort upstream
// TODO - fill in missing fields from non-archetypes
// TODO - do we want a URL per offer?  Keg vs. can vs. item may be different pages
private fun List<Item>.mergeAndPrioritiseOffers() =
  flatMap { item -> item.offers.map { ItemAndOffer(item, it) } }
    .distinctBy { round(it.offer.pricePerMl() * 100) } // Work in pence to avoid FP precision issues
    .sortedWith(compareBy(
      { it.offer.keg },   // Kegs should be lowest priority
      { it.offer.pricePerMl() },
      { it.offer.quantity } // All being equal, we prefer to buy fewer cans
    ))

private fun Offer.pricePerMl() = totalPrice / (quantity * (sizeMl ?: DEFAULT_SIZE_ML))

private data class ItemAndOffer(
  val item: Item,
  val offer: Offer
)

private data class ItemGroupFields(
  val brewery: String,
  val name: String
)
