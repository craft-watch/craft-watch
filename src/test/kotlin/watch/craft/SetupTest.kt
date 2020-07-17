package watch.craft

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import watch.craft.storage.ObjectStore

class SetupTest {
  private val firstLevel = mock<ObjectStore>()
  private val secondLevel = mock<ObjectStore>()

  @Nested
  inner class NoForceDownload {
    @Test
    fun `no existing entries for today`() {
      whenever(secondLevel.list(any())) doReturn emptyList()

      val setup = createSetup(false)

      assertEquals("downloads/2020-01-01", setup.downloadsDir.base)
    }

    @Test
    fun `one existing entry for today`() {
      whenever(secondLevel.list(any())) doReturn listOf("2020-01-01")

      val setup = createSetup(false)

      assertEquals("downloads/2020-01-01", setup.downloadsDir.base)
    }

    @Test
    fun `multiple existing entries for today`() {
      whenever(secondLevel.list(any())) doReturn listOf("2020-01-01--001", "2020-01-01")

      val setup = createSetup(false)

      assertEquals("downloads/2020-01-01--001", setup.downloadsDir.base)
    }
  }

  @Nested
  inner class ForceDownload {
    @Test
    fun `no existing entries for today`() {
      whenever(secondLevel.list(any())) doReturn emptyList()

      val setup = createSetup(true)

      assertEquals("downloads/2020-01-01", setup.downloadsDir.base)
    }

    @Test
    fun `one existing entry for today`() {
      whenever(secondLevel.list(any())) doReturn listOf("2020-01-01")

      val setup = createSetup(true)

      assertEquals("downloads/2020-01-01--001", setup.downloadsDir.base)
    }

    @Test
    fun `multiple existing entries for today`() {
      whenever(secondLevel.list(any())) doReturn listOf("2020-01-01--001", "2020-01-01")

      val setup = createSetup(true)

      assertEquals("downloads/2020-01-01--002", setup.downloadsDir.base)
    }
  }

  private fun createSetup(forceDownload: Boolean): Setup {
    return Setup(
      dateString = "2020-01-01",
      forceDownload = forceDownload,
      firstLevelStore = firstLevel,
      secondLevelStore = secondLevel
    )
  }
}
