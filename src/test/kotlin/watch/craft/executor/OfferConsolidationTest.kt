package watch.craft.executor

import com.nhaarman.mockitokotlin2.mock
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import watch.craft.Inventory
import watch.craft.Item
import watch.craft.Offer
import watch.craft.PROTOTYPE_ITEM
import java.net.URI

class OfferConsolidationTest {
  @Test
  fun `merges items with same name from same brewery`() {
    val items = listOf(
      item(offers = listOf(Offer(totalPrice = 1.00))),
      item(offers = listOf(Offer(totalPrice = 2.00)))
    )

    assertEquals(1, items.consolidate().size)
  }

  @Test
  fun `doesn't merge items with same name from different breweries`() {
    val items = listOf(
      item(offers = listOf(Offer(totalPrice = 1.00))),
      item(brewery = THAT_BREWERY, offers = listOf(Offer(totalPrice = 2.00)))
    )

    assertEquals(2, items.consolidate().size)
  }

  @Test
  fun `merges items case-insensitively`() {
    val items = listOf(
      item(offers = listOf(Offer(totalPrice = 1.00))),
      item(name = THIS_BEER.toUpperCase(), offers = listOf(Offer(totalPrice = 2.00)))
    )

    assertEquals(1, items.consolidate().size)
  }

  @Test
  fun `sorts offers by best price per ml`() {
    val baseline = Offer(quantity = 1, totalPrice = 4.00, sizeMl = 330)
    val higherQuantity = baseline.copy(quantity = 2)
    val lowerPrice = baseline.copy(totalPrice = 3.00)
    val biggerSize = baseline.copy(sizeMl = 750)

    val items = listOf(
      item(offers = listOf(
        baseline,
        higherQuantity,
        lowerPrice,
        biggerSize
      ))
    )

    assertEquals(
      listOf(
        biggerSize,
        higherQuantity,
        lowerPrice,
        baseline
      ),
      items.consolidate().single().offers
    )
  }

  @Test
  fun `kegs sort to bottom, even if price per ml is better than non-keg`() {
    val nonKeg = Offer(quantity = 1, totalPrice = 1.00, sizeMl = 500)
    val keg = Offer(quantity = 1, totalPrice = 1.00, sizeMl = 1000, keg = true) // Slightly unrealistic...


    val items = listOf(
      item(offers = listOf(
        nonKeg,
        keg
      ))
    )

    assertEquals(
      listOf(
        nonKeg,
        keg
      ),
      items.consolidate().single().offers
    )
  }

  @Test
  fun `assumes default if size unknown`() {
    val baseline = Offer(quantity = 1, totalPrice = 4.00, sizeMl = 440)
    val unknownSize = baseline.copy(sizeMl = null)
    val tiny = baseline.copy(sizeMl = 220)

    val items = listOf(
      item(offers = listOf(
        baseline,
        tiny,
        unknownSize
      ))
    )

    assertEquals(
      listOf(
        baseline,
        unknownSize,
        tiny
      ),
      items.consolidate().single().offers
    )
  }

  @Test
  fun `merges offers with same price per ml by selecting lowest quantity`() {
    val a = Offer(quantity = 1, totalPrice = 1.00)
    val b = Offer(quantity = 2, totalPrice = 2.00)
    val c = Offer(quantity = 4, totalPrice = 4.00)

    val items = listOf(
      item(offers = listOf(
        a,
        c,
        b
      ))
    )

    assertEquals(
      listOf(
        a
      ),
      items.consolidate().single().offers
    )
  }

  @Test
  fun `doesn't merge keg and non-keg offers with same price per ml`() {
    val nonKeg = Offer(quantity = 1, totalPrice = 1.00)
    val keg = Offer(quantity = 2, totalPrice = 2.00, keg = true)  // Slightly unrealistic...

    val items = listOf(
      item(offers = listOf(
        nonKeg,
        keg
      ))
    )

    assertEquals(2, items.consolidate().single().offers.size)
  }

  @Test
  fun `merges identical offers by picking item with lexicographically first URL`() {
    val items = listOf(
      item(offers = listOf(Offer(totalPrice = 1.00))).copy(url = URI("https://example.invalid/b")),
      item(offers = listOf(Offer(totalPrice = 1.00))).copy(url = URI("https://example.invalid/a"))
    )

    assertEquals(
      URI("https://example.invalid/a"),
      items.consolidate().single().url
    )
  }

  @Test
  fun `headline offer is consistent with selected item`() {
    val items = listOf(
      item(offers = listOf(Offer(totalPrice = 1.00))).copy(desc = "Hello"),
      item(offers = listOf(Offer(totalPrice = 2.00))).copy(desc = "Goodbye")
    )

    assertEquals("Hello", items.consolidate().first().desc)
  }

  @Test
  fun `handles no items`() {
    assertEquals(0, emptyList<Item>().consolidate().size)
  }

  // This should never happen, but let's handle it gracefully anyway
  @Test
  fun `handles item with no offers`() {
    assertEquals(0, listOf(item(offers = emptyList())).consolidate().size)
  }

  private fun List<Item>.consolidate() = toInventory().consolidateOffers().items

  private fun item(
    brewery: String = THIS_BREWERY,
    name: String = THIS_BEER,
    offers: List<Offer>
  ) = PROTOTYPE_ITEM.copy(
    brewery = brewery,
    name = name,
    offers = offers
  )

  private fun List<Item>.toInventory() = Inventory(
    metadata = mock(),
    categories = emptyList(),
    breweries = emptyList(),
    items = this
  )

  companion object {
    private const val THIS_BREWERY = "ABC Brew"
    private const val THAT_BREWERY = "DEF Brew"
    private const val THIS_BEER = "Foo"
  }
}
