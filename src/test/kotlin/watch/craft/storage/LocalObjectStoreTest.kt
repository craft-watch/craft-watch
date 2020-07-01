package watch.craft.storage

import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path

class LocalObjectStoreTest {
  @Test
  fun `writes data to filesystem if not already present`(@TempDir tempDir: Path) {
    val store = LocalObjectStore(tempDir.toFile())

    store.write(NICE_KEY, NICE_DATA)

    assertArrayEquals(NICE_DATA, tempDir.resolve(NICE_KEY).toFile().readBytes())
  }

  @Test
  fun `throws and doesn't overwrite if file already present`(@TempDir tempDir: Path) {
    tempDir.resolve(NICE_KEY).toFile().writeBytes(NICE_DATA)
    val store = LocalObjectStore(tempDir.toFile())

    assertThrows<FileExistsException> {
      store.write(NICE_KEY, OTHER_DATA)
    }
    assertArrayEquals(NICE_DATA, tempDir.resolve(NICE_KEY).toFile().readBytes())
  }

  @Test
  fun `reads data from filesystem if present`(@TempDir tempDir: Path) {
    tempDir.resolve(NICE_KEY).toFile().writeBytes(NICE_DATA)
    val store = LocalObjectStore(tempDir.toFile())

    assertArrayEquals(NICE_DATA, store.read(NICE_KEY))
  }

  @Test
  fun `throws if file not present`(@TempDir tempDir: Path) {
    val store = LocalObjectStore(tempDir.toFile())

    assertThrows<FileDoesntExistException> {
      store.read(NICE_KEY)
    }
  }

  companion object {
    private const val NICE_KEY = "foo"
    private val NICE_DATA = byteArrayOf(1, 2, 3, 4)
    private val OTHER_DATA = byteArrayOf(5, 6, 7, 8)
  }
}
