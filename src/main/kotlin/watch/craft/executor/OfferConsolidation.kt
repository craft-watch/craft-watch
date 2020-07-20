package watch.craft.executor

import mu.KotlinLogging
import watch.craft.DEFAULT_SIZE_ML
import watch.craft.Format.KEG
import watch.craft.Item
import watch.craft.Offer
import kotlin.math.round

private val logger = KotlinLogging.logger {}

fun StatsWith<Item>.consolidateOffers(): StatsWith<Item> {
  var numMerged = stats.numMerged!!
  val entries = entries
    .groupBy { ItemGroupFields(it.breweryId, it.name.toLowerCase()) }
    .mapNotNull { (key, group) ->
      if (group.size > 1) {
        logger.info("[${key.brewery}] Merging ${group.size} item(s) for [${key.name}]")
        numMerged += group.size - 1
      }

      val offers = group.mergeAndPrioritiseOffers()
      if (offers.isNotEmpty()) {
        val headlineItem = offers.selectHeadlineItem()
        headlineItem.copy(offers = offers.map { it.offer })
      } else {
        null
      }
    }
  return StatsWith(
    entries = entries,
    stats = stats.copy(numMerged = numMerged)
  )
}

// TODO - fill in missing fields from non-archetypes
// TODO - do we want a URL per offer?  Keg vs. can vs. item may be different pages
private fun List<Item>.mergeAndPrioritiseOffers() =
  flatMap { item -> item.offers.map { ItemAndOffer(item, it) } }
    .sortedWith(compareBy(
      { it.offer.format == KEG },   // Kegs should be lowest priority, as most users likely care most about cans/bottles
      { it.offer.pricePerMl() },
      { it.offer.quantity }, // All being equal, we prefer to buy fewer cans
      { it.item.url } // We need a fallback to ensure deterministic behaviour
    ))
    .distinctBy {
      Pair(
        it.offer.format == KEG,                         // Don't merge kegs and non-kegs, as users may want to make this choice
        round(it.offer.pricePerDefaultItem() * 100)  // Work in pence to avoid FP precision issues
      )
    }

// Individual items typically have better descriptions + thumbnail
private fun List<ItemAndOffer>.selectHeadlineItem() =
  (firstOrNull { it.offer.quantity == 1 && it.offer.format != KEG } ?: first()).item

private fun Offer.pricePerDefaultItem() = pricePerMl() * DEFAULT_SIZE_ML
private fun Offer.pricePerMl() = totalPrice / (quantity * (sizeMl ?: DEFAULT_SIZE_ML))

private data class ItemAndOffer(
  val item: Item,
  val offer: Offer
)

private data class ItemGroupFields(
  val brewery: String,
  val name: String
)
