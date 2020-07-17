package watch.craft.executor

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import watch.craft.BreweryStats
import watch.craft.Format.KEG
import watch.craft.Item
import watch.craft.Offer
import watch.craft.PROTOTYPE_ITEM
import java.net.URI

class OfferConsolidationTest {
  @Test
  fun `merges items with same name from same brewery`() {
    val items = listOf(
      item(offers = listOf(Offer(totalPrice = 1.00))),
      item(offers = listOf(Offer(totalPrice = 2.00))),
      item(offers = listOf(Offer(totalPrice = 3.00)))
    )

    val ret = items.consolidate()

    assertEquals(1, ret.entries.size)
    assertEquals(2, ret.stats.numMerged)
  }

  @Test
  fun `doesn't merge items with same name from different breweries`() {
    val items = listOf(
      item(offers = listOf(Offer(totalPrice = 1.00))),
      item(breweryId = THAT_BREWERY_ID, offers = listOf(Offer(totalPrice = 2.00)))
    )

    val ret = items.consolidate()

    assertEquals(2, ret.entries.size)
    assertEquals(0, ret.stats.numMerged)
  }

  @Test
  fun `merges items case-insensitively`() {
    val items = listOf(
      item(offers = listOf(Offer(totalPrice = 1.00))),
      item(name = THIS_BEER.toUpperCase(), offers = listOf(Offer(totalPrice = 2.00)))
    )

    assertEquals(1, items.consolidate().entries.size)
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
      items.consolidate().entries.single().offers
    )
  }

  @Test
  fun `kegs sort to bottom, even if price per ml is better than non-keg`() {
    val nonKeg = Offer(quantity = 1, totalPrice = 1.00, sizeMl = 500)
    val keg = Offer(quantity = 1, totalPrice = 1.00, sizeMl = 1000, format = KEG) // Slightly unrealistic...


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
      items.consolidate().entries.single().offers
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
      items.consolidate().entries.single().offers
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
      items.consolidate().entries.single().offers
    )
  }

  @Test
  fun `doesn't merge keg and non-keg offers with same price per ml`() {
    val nonKeg = Offer(quantity = 1, totalPrice = 1.00)
    val keg = Offer(quantity = 2, totalPrice = 2.00, format = KEG)  // Slightly unrealistic...

    val items = listOf(
      item(offers = listOf(
        nonKeg,
        keg
      ))
    )

    assertEquals(2, items.consolidate().entries.single().offers.size)
  }

  @Test
  fun `merges identical offers by picking item with lexicographically first URL`() {
    val items = listOf(
      item(offers = listOf(Offer(totalPrice = 1.00))).copy(url = URI("https://example.invalid/b")),
      item(offers = listOf(Offer(totalPrice = 1.00))).copy(url = URI("https://example.invalid/a"))
    )

    assertEquals(
      URI("https://example.invalid/a"),
      items.consolidate().entries.single().url
    )
  }

  @Nested
  inner class InfoSelection {
    @Test
    fun `if headline offer is single-item, then headline selected`() {
      val items = listOf(
        item(offers = listOf(Offer(totalPrice = 1.00))).copy(desc = "Hello"),
        item(offers = listOf(Offer(totalPrice = 2.00))).copy(desc = "Goodbye")
      )

      assertEquals("Hello", items.consolidate().entries.first().desc)
    }

    @Test
    fun `if headline is multi-pack then selects single-item info if available`() {
      val items = listOf(
        item(offers = listOf(Offer(totalPrice = 1.00, quantity = 4))).copy(desc = "Multi-pack"),
        item(offers = listOf(Offer(totalPrice = 2.00, quantity = 1))).copy(desc = "Single item")
      )

      val item = items.consolidate().entries.single()

      assertEquals(4, item.offers.first().quantity)
      assertEquals("Single item", item.desc)
    }

    @Test
    fun `if headline is multi-pack and no single-items available, then headline selected`() {
      val items = listOf(
        item(offers = listOf(Offer(totalPrice = 1.00, quantity = 4))).copy(desc = "Multi-pack"),
        item(offers = listOf(Offer(totalPrice = 2.00, quantity = 2))).copy(desc = "Not a single item")
      )

      val item = items.consolidate().entries.single()

      assertEquals(4, item.offers.first().quantity)
      assertEquals("Multi-pack", item.desc)
    }

    @Test
    fun `if headline is multi-pack and only keg single-items available, then headline selected`() {
      val items = listOf(
        item(offers = listOf(Offer(totalPrice = 1.00, quantity = 4))).copy(desc = "Multi-pack"),
        item(offers = listOf(Offer(totalPrice = 2.00, format = KEG))).copy(desc = "Gross keg")
      )

      val item = items.consolidate().entries.single()

      assertEquals(4, item.offers.first().quantity)
      assertEquals("Multi-pack", item.desc)
    }
  }

  @Nested
  inner class EdgeCases {
    @Test
    fun `handles no items`() {
      assertEquals(0, emptyList<Item>().consolidate().entries.size)
    }

    // This should never happen, but let's handle it gracefully anyway
    @Test
    fun `handles item with no offers`() {
      assertEquals(0, listOf(item(offers = emptyList())).consolidate().entries.size)
    }
  }

  private fun item(
    breweryId: String = THIS_BREWERY_ID,
    name: String = THIS_BEER,
    offers: List<Offer>
  ) = PROTOTYPE_ITEM.copy(
    breweryId = breweryId,
    name = name,
    offers = offers
  )

  private fun List<Item>.consolidate() =
    StatsWith(this, BreweryStats("whatever")).consolidateOffers()

  companion object {
    private const val THIS_BREWERY_ID = "abc"
    private const val THAT_BREWERY_ID = "def"
    private const val THIS_BEER = "Foo"
  }
}
