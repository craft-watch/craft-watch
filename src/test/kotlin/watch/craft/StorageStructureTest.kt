package watch.craft

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import watch.craft.storage.ObjectStore

class StorageStructureTest {
  private val firstLevel = mock<ObjectStore>()
  private val secondLevel = mock<ObjectStore>()

  @Nested
  inner class NoForceDownload {
    @Test
    fun `no existing entries for today`() {
      whenever(secondLevel.list(any())) doReturn emptyList()

      val structure = createStructure(false)

      assertEquals("/downloads/foo/2020-01-01", structure.downloads("foo").path)
    }

    @Test
    fun `one existing entry for today`() {
      whenever(secondLevel.list(any())) doReturn listOf("2020-01-01")

      val structure = createStructure(false)

      assertEquals("/downloads/foo/2020-01-01", structure.downloads("foo").path)
    }

    @Test
    fun `multiple existing entries for today`() {
      whenever(secondLevel.list(any())) doReturn listOf("2020-01-01--001", "2020-01-01")

      val structure = createStructure(false)

      assertEquals("/downloads/foo/2020-01-01--001", structure.downloads("foo").path)
    }
  }

  @Nested
  inner class ForceDownload {
    @Test
    fun `no existing entries for today`() {
      whenever(secondLevel.list(any())) doReturn emptyList()

      val structure = createStructure(true)

      assertEquals("/downloads/foo/2020-01-01", structure.downloads("foo").path)
    }

    @Test
    fun `one existing entry for today`() {
      whenever(secondLevel.list(any())) doReturn listOf("2020-01-01")

      val structure = createStructure(true)

      assertEquals("/downloads/foo/2020-01-01--001", structure.downloads("foo").path)
    }

    @Test
    fun `multiple existing entries for today`() {
      whenever(secondLevel.list(any())) doReturn listOf("2020-01-01--001", "2020-01-01")

      val structure = createStructure(true)

      assertEquals("/downloads/foo/2020-01-01--002", structure.downloads("foo").path)
    }
  }

  private fun createStructure(forceDownload: Boolean) = StorageStructure(
    dateString = "2020-01-01",
    forceDownload = forceDownload,
    firstLevelStore = firstLevel,
    secondLevelStore = secondLevel
  )
}
