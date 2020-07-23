package watch.craft.dsl

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import watch.craft.MalformedInputException

class SemanticsTest {
  @Nested
  inner class QuantityFrom {
    @Test
    fun `with prefix`() {
      assertEquals(6, "x6".quantityFrom())
      assertEquals(6, "x 6".quantityFrom())
      assertEquals(66, "x  66".quantityFrom())
      assertEquals(6, "X6".quantityFrom())
    }

    @Test
    fun `with suffix`() {
      assertEquals(6, "6x".quantityFrom())
      assertEquals(6, "6 x".quantityFrom())
      assertEquals(66, "66  x".quantityFrom())
      assertEquals(6, "6X".quantityFrom())
    }

    @Test
    fun `other variants`() {
      assertEquals(6, "×6".quantityFrom())
      assertEquals(6, "6×".quantityFrom())
      assertEquals(6, "6 pack".quantityFrom())
      assertEquals(6, "6-pack".quantityFrom())
    }

    @Test
    fun `user-supplied regex`() {
      assertEquals(6, "case of 6".quantityFrom("case of (\\d+)"))
    }

    @Test
    fun `true negatives`() {
      assertThrows<MalformedInputException> { "6".quantityFrom() }
      assertThrows<MalformedInputException> { "blah 6 blah".quantityFrom() }
    }
  }

  @Nested
  inner class SizeFrom {
    @Test
    fun millilitres() {
      assertEquals(550, "550 ml".sizeMlFrom())
      assertEquals(550, "550 mL".sizeMlFrom())
      assertEquals(550, "550 ML".sizeMlFrom())
      assertEquals(550, "550   ml".sizeMlFrom())
      assertEquals(550, "550ml".sizeMlFrom())
    }

    @Test
    fun litres() {
      assertEquals(3000, "3 litre".sizeMlFrom())
      assertEquals(3000, "3 litres".sizeMlFrom())
      assertEquals(3000, "3 liter".sizeMlFrom())
      assertEquals(3000, "3 liters".sizeMlFrom())
      assertEquals(3000, "3L".sizeMlFrom())
      assertEquals(3000, "3l".sizeMlFrom())
      assertEquals(3000, "3-litre".sizeMlFrom())
      assertEquals(3500, "3.5 litre".sizeMlFrom())
    }

    @Test
    fun `true negatives`() {
      assertThrows<MalformedInputException> { "3 llamas".sizeMlFrom() }
      assertThrows<MalformedInputException> { "550 mlady".sizeMlFrom() }
    }
  }
}
